# ----------------------------------------------------
# 1. 导入必要的库 (新增 json)
# ----------------------------------------------------
import pandas as pd
import numpy as np
import pickle
import os
import sys
import argparse
from datetime import datetime, timedelta
from typing import Any, Dict, Optional, List
import warnings
import tensorflow as tf
import keras
import json  # <-- 新增 JSON 库

# 忽略 Pandas 性能警告等不影响逻辑的警告
warnings.filterwarnings('ignore')

# ----------------------------------------------------
# 2. 配置和常量定义 (保持不变)
# ----------------------------------------------------
# 数据和模型结果的路径（根据实际项目结构调整）
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

# 数据和模型结果的相对路径（相对于 LSTM50 目录）
DATA_DIR = os.path.join(BASE_DIR, 'data50')
MODEL_RESULTS_DIR = os.path.join(BASE_DIR, 'product_allPredictions')
# --- 文件名 ---
PICKLE_FILE_NAME = 'lstm_data_collection.pkl'

# --- 清洗和模型常量 (必须与清洗脚本一致) ---
FILL_VALUE = 'UNKNOWN'
CATEGORICAL_COLS = ['specInfo', 'prodPcat']
PRICE_COL_NAME = 'avgPrice'
LOOK_BACK = 14
LAG_FEATURES = [1, 7, 30, 365]
MA_WINDOW = 30
MAX_PRODUCTS_TO_TRAIN = 50  # 限制加载的资产数量


# ----------------------------------------------------
# 3. 辅助函数
# ----------------------------------------------------

def load_lstm_data(file_path: str) -> Optional[dict[str, dict[str, Any]]]:
    """从磁盘加载 lstm_data_collection 字典。"""
    if os.path.exists(file_path):
        try:
            with open(file_path, 'rb') as f:
                # 确保加载的对象结构是预期的
                return pickle.load(f)
        except Exception as e:
            print(f"致命错误：加载数据文件 {file_path} 失败：{e}", file=sys.stderr)
            return None
    else:
        # 注意：这里 file_path 已经是拼接好的相对路径
        print(f"致命错误：未找到数据文件 {file_path}。请检查路径是否正确。", file=sys.stderr)
        return None


def get_required_assets(
        lstm_data_collection: Dict[str, Dict[str, Any]],
        target_product: str
) -> Optional[Dict[str, Any]]:
    """获取目标产品的模型、Scaler、特征列、类别映射和历史DataFrame。"""
    if target_product not in lstm_data_collection:
        print(f"错误：产品 '{target_product}' 的数据资产未在PKL文件中找到。", file=sys.stderr)
        return None

    data_entry = lstm_data_collection[target_product]

    # 使用 os.path.join 拼接，路径基于新的 MODEL_RESULTS_DIR
    safe_prod_name = "".join(c for c in target_product if c.isalnum() or c in (' ', '_')).rstrip()
    model_path = os.path.join(MODEL_RESULTS_DIR, f'{safe_prod_name}_lstm_model.keras')
    scaler_path = os.path.join(MODEL_RESULTS_DIR, f'{safe_prod_name}_price_scaler.pkl')

    if not (os.path.exists(model_path) and os.path.exists(scaler_path)):
        print(f"错误：找不到 {target_product} 的模型或价格Scaler文件。尝试路径: {model_path}", file=sys.stderr)
        return None

    try:
        model = keras.models.load_model(model_path)
        with open(scaler_path, 'rb') as f:
            price_scaler = pickle.load(f)

        return {
            'model': model,
            'price_scaler': price_scaler,
            'combined_scaler': data_entry['combined_scaler'],
            'features': data_entry['features'],
            'category_mappings': data_entry['category_mappings'],
            'history_df': data_entry['df_processed']
        }
    except Exception as e:
        print(f"错误：加载 {target_product} 的模型/Scaler/CombinedScaler 失败: {e}", file=sys.stderr)
        return None


def get_product_specific_metadata(
        lstm_data_collection: Dict[str, Dict[str, Any]],
        target_product: str
) -> Optional[Dict[str, List[str]]]:
    """
    **新增**：获取特定产品的规格和类别映射。
    """
    if target_product not in lstm_data_collection:
        return None

    data_entry = lstm_data_collection[target_product]
    mappings = data_entry.get('category_mappings')

    if mappings and isinstance(mappings, dict):
        return {
            "specs": sorted(list(set(mappings.get('specInfo', [])))),
            "pcats": sorted(list(set(mappings.get('prodPcat', []))))
        }
    return None


def generate_single_feature_row_static(
        date: datetime,
        target_spec: str,
        target_pcat: str,
        feature_cols: List[str],
        category_mappings: Dict[str, Any]
) -> Optional[pd.Series]:
    """为单个未来日期生成当前时间步的静态特征 (时序 + OHE)。"""
    # 时间特征
    row_data = {
        'day_of_year': date.dayofyear,
        'day_of_week': date.dayofweek,
        'month': date.month,
        'doy_sin': np.sin(2 * np.pi * date.dayofyear / 365.0),
        'doy_cos': np.cos(2 * np.pi * date.dayofyear / 365.0),
    }

    # 分类特征 (OHE)
    for col in CATEGORICAL_COLS:
        cat_val = target_spec if col == 'specInfo' else target_pcat
        categories = category_mappings.get(col, [])
        for cat in categories:
            col_name = f"{col}_{cat}"
            row_data[col_name] = 1.0 if cat_val == cat else 0.0

    final_row = pd.Series(index=feature_cols, dtype=float)

    for k, v in row_data.items():
        if k in final_row.index:
            final_row[k] = v

    final_row = final_row.fillna(0.0)

    for col in feature_cols:
        if col.startswith(PRICE_COL_NAME):
            final_row[col] = 0.0

    return final_row


def create_lstm_input_for_future_date(
        date: datetime,
        history_price_series: pd.Series,
        target_spec: str,
        target_pcat: str,
        feature_cols: List[str],
        category_mappings: Dict[str, Any],
        price_scaler: Any
) -> Optional[np.ndarray]:
    """为未来的单个时间步生成一个 LOOK_BACK 长度的 LSTM 输入矩阵。"""

    # 1. 构造当前时间步的静态特征 (时序 + OHE)
    static_features = generate_single_feature_row_static(
        date=date,
        target_spec=target_spec,
        target_pcat=target_pcat,
        feature_cols=feature_cols,
        category_mappings=category_mappings
    )

    # 2. 动态计算当前步 (T+1) 的 Lag/MA (基于最新的 history_price_series)
    current_lag_ma = {}

    if len(history_price_series) < max(LAG_FEATURES + [MA_WINDOW]):
        print(f"警告：历史价格序列长度 ({len(history_price_series)}) 不足，Lag/MA 设为 0.0。", file=sys.stderr)
        for lag in LAG_FEATURES:
            current_lag_ma[f'{PRICE_COL_NAME}_lag_{lag}'] = 0.0
        current_lag_ma[f'{PRICE_COL_NAME}_MA_{MA_WINDOW}'] = 0.0
    else:
        # 计算 Lag 特征 (使用历史序列的最新值)
        for lag in LAG_FEATURES:
            lag_col = f'{PRICE_COL_NAME}_lag_{lag}'
            current_lag_ma[lag_col] = history_price_series.iloc[-lag]

        # 计算 MA 特征
        ma_col = f'{PRICE_COL_NAME}_MA_{MA_WINDOW}'
        current_lag_ma[ma_col] = history_price_series.iloc[-MA_WINDOW:].mean()

    # 3. 合并静态特征和动态 Lag/MA 以构造当前预测步的完整特征向量
    current_input_row = static_features.copy()

    for k, v in current_lag_ma.items():
        if k in current_input_row.index:
            current_input_row[k] = v

    current_input_row = current_input_row.fillna(0.0)




    # 4. 构造 LOOK_BACK 序列 X
    X_matrix_dynamic = np.zeros((LOOK_BACK, len(feature_cols)))

    try:
        price_idx = feature_cols.index(PRICE_COL_NAME)
    except ValueError:
        print("致命错误：价格列未在特征列表中找到！", file=sys.stderr)
        return None

    num_history_points = len(history_price_series)
    history_to_use = min(num_history_points, LOOK_BACK - 1)

    # 填充 LOOK_BACK-1 行历史数据 (使用 price_scaler 归一化历史价格)
    for r in range(history_to_use):
        # 1. 归一化历史价格 (使用 price_scaler，因为它只接受 1 个特征)
        scaled_price_val = price_scaler.transform(np.array([[history_price_series.iloc[r]]]))[0, 0]

        for c_idx, col_name in enumerate(feature_cols):
            if col_name.startswith(PRICE_COL_NAME):
                X_matrix_dynamic[r, c_idx] = scaled_price_val
            else:
                # 非价格特征：使用 T+1 时刻的静态特征作为近似
                X_matrix_dynamic[r, c_idx] = static_features.get(col_name, 0.0)

    # 填充最后一行 (当前预测时间步 T+1 的完整特征向量)
    X_matrix_dynamic[-1, :] = current_input_row.values

    # 5. 增加批次维度 (Batch=1)
    X_matrix_with_batch = np.expand_dims(X_matrix_dynamic, axis=0)

    return X_matrix_with_batch


# ----------------------------------------------------
# 4. 主逻辑函数
# ----------------------------------------------------

def main():
    # 添加命令行参数解析，新增 --mode 和 --productName (用于查询)
    parser = argparse.ArgumentParser(description='农产品价格预测或元数据查询脚本')
    parser.add_argument('--mode', required=True, choices=['PREDICT', 'QUERY_META', 'VALIDATE_ALL'],
                        help='脚本运行模式: PREDICT (预测), QUERY_META (查询特定产品元数据), VALIDATE_ALL (校验所有产品)')

    # 预测和查询都需要的参数 (用于加载数据资产)
    parser.add_argument('--product', default=None, help='产品名称 (用于PREDICT或QUERY_META)')

    # 预测模式必需的参数
    parser.add_argument('--pcat', default=None, help='产品类别 (prodPcat)')
    parser.add_argument('--spec', default=None, help='产品规格 (specInfo)')
    parser.add_argument('--end-date', default=None, help='预测截止日期 (格式：YYYY-MM-DD)')

    args = parser.parse_args()

    # 1. 加载数据
    # 使用 os.path.join 确保跨平台兼容性
    data_path = os.path.join(DATA_DIR, PICKLE_FILE_NAME)

    # 尝试加载数据
    loaded_data = load_lstm_data(data_path)

    # 确保加载成功且包含 'data' 键
    if loaded_data is None or 'data' not in loaded_data:
        sys.exit(1)

    lstm_data_collection = loaded_data['data']

    if args.mode == 'PREDICT':
        # --- 模式 1: 价格预测 (与原代码逻辑基本一致) ---
        target_prod_name = args.product
        if target_prod_name not in lstm_data_collection:
            print(f"错误：产品 '{target_prod_name}' 不在已训练模型列表中", file=sys.stderr)
            sys.exit(1)

        assets = get_required_assets(lstm_data_collection, target_prod_name)
        if not assets:
            sys.exit(1)

        target_spec = args.spec
        target_pcat = args.pcat

        # 筛选历史数据
        spec_col_name = f"specInfo_{target_spec}"
        pcat_col_name = f"prodPcat_{target_pcat}"

        if spec_col_name not in assets['features'] or pcat_col_name not in assets['features']:
            print(f"错误：规格或类别的OHE列不在特征列表中", file=sys.stderr)
            sys.exit(1)

        try:
            filter_condition = (assets['history_df'][spec_col_name] == 1.0) & \
                               (assets['history_df'][pcat_col_name] == 1.0)
            spec_history_df = assets['history_df'][filter_condition].sort_index()

            if spec_history_df.empty:
                print(f"错误：找不到该产品的历史价格数据", file=sys.stderr)
                sys.exit(1)

            price_history_series = spec_history_df[PRICE_COL_NAME]

        except Exception as e:
            print(f"筛选历史数据时发生错误: {e}", file=sys.stderr)
            sys.exit(1)

        if price_history_series.empty:
            print("错误：筛选后的历史价格序列为空", file=sys.stderr)
            sys.exit(1)

        # 处理预测日期
        try:
            end_date = datetime.strptime(args.end_date, '%Y-%m-%d')
            latest_date = price_history_series.index.max()

            if end_date <= latest_date:
                print(f"错误：预测截止日期必须晚于最新数据日期 {latest_date.strftime('%Y-%m-%d')}", file=sys.stderr)
                sys.exit(1)

            num_forecast_days = (end_date - latest_date).days
            forecast_dates = [latest_date + timedelta(days=i + 1) for i in range(num_forecast_days)]

        except ValueError as e:
            print(f"日期格式错误: {e}", file=sys.stderr)
            sys.exit(1)

        # 执行自回归预测
        current_price_history = list(price_history_series.values)

        for current_date in forecast_dates:
            X_matrix = create_lstm_input_for_future_date(
                date=current_date,
                history_price_series=pd.Series(current_price_history),
                target_spec=target_spec,
                target_pcat=target_pcat,
                feature_cols=assets['features'],
                category_mappings=assets['category_mappings'],
                price_scaler=assets['price_scaler']
            )

            if X_matrix is None:
                print(f"无法为 {current_date.strftime('%Y-%m-%d')} 生成输入矩阵", file=sys.stderr)
                sys.exit(1)

            try:
                scaled_prediction = assets['model'].predict(X_matrix, verbose=0)[0]
                predicted_price_actual = assets['price_scaler'].inverse_transform(scaled_prediction.reshape(-1, 1))[0, 0]
                current_price_history.append(predicted_price_actual)

                # 输出 CSV 格式结果
                print(f"{current_date.strftime('%Y-%m-%d')},{predicted_price_actual:.2f}")

            except Exception as e:
                print(f"预测 {current_date.strftime('%Y-%m-%d')} 时出错: {e}", file=sys.stderr)
                sys.exit(1)

        sys.exit(0)


    elif args.mode == 'QUERY_META':
        # --- 模式 2: 查询特定产品元数据 ---
        target_prod_name = args.product
        if not target_prod_name:
            print("错误：在 QUERY_META 模式下，必须提供 --product 参数。", file=sys.stderr)
            sys.exit(1)

        # 1. 提取所有产品元数据
        all_products_meta = {}
        # lstm_data_collection 已经是 data 字典本身
        for prod_name, prod_data in lstm_data_collection.items():
            mappings = prod_data.get('category_mappings')
            if mappings and isinstance(mappings, dict):
                all_products_meta[prod_name.strip()] = {
                    "specs": sorted(list(set(mappings.get('specInfo', [])))),
                    "pcats": sorted(list(set(mappings.get('prodPcat', []))))
                }

        # 2. 查找目标产品
        result_meta = all_products_meta.get(target_prod_name.strip())

        if result_meta:
            # 3. 以 JSON 格式输出，供 Java 端解析
            print(json.dumps(result_meta, ensure_ascii=False, indent=2))
            sys.exit(0)
        else:
            print(f"错误：在 PKL 数据中未找到产品 '{target_prod_name}' 的元数据。", file=sys.stderr)
            sys.exit(1)


    elif args.mode == 'VALIDATE_ALL':
        # --- 模式 3: 校验所有产品元数据 ---
        all_specs = set()
        all_pcats = set()

        # lstm_data_collection 已经是 data 字典本身
        for prod_data in lstm_data_collection.values():
            if 'category_mappings' in prod_data:
                mappings = prod_data['category_mappings']
                all_specs.update(mappings.get('specInfo', []))
                all_pcats.update(mappings.get('prodPcat', []))

        metadata_output = {
            "specs": sorted(list(all_specs)),
            "pcats": sorted(list(all_pcats))
        }
        # 输出 JSON 格式
        print(json.dumps(metadata_output, ensure_ascii=False, indent=2))
        sys.exit(0)

# ----------------------------------------------------
# 5. 执行入口
# ----------------------------------------------------
if __name__ == "__main__":
    main()