# ----------------------------------------------------
# 文件名: prediction_now.py
# 功能: Prophet 预测脚本 (修正路径版)
# ----------------------------------------------------
import pandas as pd
import numpy as np
import pickle
import os
import sys
import argparse
from datetime import datetime, timedelta
import json
from prophet import Prophet
import logging
import matplotlib.pyplot as plt

# 压制 Prophet 的控制台日志
logging.getLogger('cmdstanpy').setLevel(logging.WARNING)
import warnings
warnings.filterwarnings('ignore')

# ----------------------------------------------------
# 1. 路径配置 (核心修改点：根据你的实际文件位置修正)
# ----------------------------------------------------
BASE_DIR = os.path.dirname(os.path.abspath(__file__))


MODEL_RESULTS_DIR = os.path.join(BASE_DIR, 'product_allPredictions')

# 【修正 2】假设你的数据文件在 LSTM50/data50 下 (如果不是，请手动修改这里)
DATA_DIR = os.path.join(BASE_DIR, 'data50')

# 元数据文件名
META_FILE_NAME = 'lstm_data_collection.pkl'


# ----------------------------------------------------
# 2. 辅助函数
# ----------------------------------------------------

def get_product_models(prod_name):
    """
    尝试加载指定产品的模型包
    """
    safe_name = "".join(c for c in prod_name if c.isalnum() or c in (' ', '_')).rstrip()

    # 拼接路径
    path = os.path.join(MODEL_RESULTS_DIR, f"{safe_name}_prophet_pack.pkl")

    # 调试打印 (如果还找不到，可以把下面这行注释取消，看看它到底在找哪里)
    # print(f"DEBUG: 正在查找文件: {path}", file=sys.stderr)

    if os.path.exists(path):
        try:
            with open(path, 'rb') as f:
                return pickle.load(f)
        except Exception:
            return None
    return None


def load_metadata():
    """加载元数据文件"""
    path = os.path.join(DATA_DIR, META_FILE_NAME)
    if os.path.exists(path):
        try:
            with open(path, 'rb') as f: return pickle.load(f)
        except Exception:
            return None
    return None


def return_missing_json(msg):
    """
    统一输出缺失数据的 JSON 格式，并正常退出
    """
    res = {
        "status": "failed",
        "reason": "data_unavailable",
        "msg": msg
    }
    print(json.dumps(res, ensure_ascii=False))
    sys.exit(0)


# ----------------------------------------------------
# 3. 主程序
# ----------------------------------------------------

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--mode', required=True, choices=['PREDICT', 'QUERY_META', 'VALIDATE_ALL'])
    parser.add_argument('--product', default=None)
    parser.add_argument('--pcat', default=None)
    parser.add_argument('--spec', default=None)
    parser.add_argument('--end-date', default=None)
    args = parser.parse_args()

    # ==========================================
    # 模式 1: PREDICT (预测)
    # ==========================================
    if args.mode == 'PREDICT':
        if not args.product or not args.end_date or not args.spec or not args.pcat:
            print("❌ 错误：缺少必要参数 (product, spec, pcat, end-date)", file=sys.stderr)
            sys.exit(1)

        # 1. 加载模型包
        models_pack = get_product_models(args.product)

        # 如果找不到产品模型文件
        if not models_pack:
            return_missing_json(f"未找到产品 '{args.product}' 的预测模型，缺少历史数据")

        # 2. 查找特定规格的模型
        combo_key = f"{args.spec}__{args.pcat}"
        target_asset = models_pack.get(combo_key)

        # 如果找不到特定规格
        if not target_asset:
            return_missing_json(f"未找到规格 '{args.spec}/{args.pcat}' 的预测模型，缺少历史数据")

        # --- 模型存在，准备预测 ---
        model = target_asset['model']
        known_months = target_asset['known_months']
        last_date = target_asset['last_date']

        # 3. 计算预测天数
        try:
            target_end = datetime.strptime(args.end_date, '%Y-%m-%d')
        except ValueError:
            print(f"❌ 错误：日期格式错误 {args.end_date}", file=sys.stderr)
            sys.exit(1)

        if target_end <= last_date:
            return_missing_json(f"目标日期 {args.end_date} 早于历史最新日期，无需预测")

        days_to_predict = (target_end - last_date).days

        # 4. Prophet 预测
        future = model.make_future_dataframe(periods=days_to_predict)
        forecast = model.predict(future)

        # 5. 过滤并输出
        future_forecast = forecast[forecast['ds'] > last_date].copy()

        has_output = False

        for index, row in future_forecast.iterrows():
            curr_date = row['ds']
            curr_month = curr_date.month
            price = row['yhat']

            # 【核心逻辑】只输出已知月份的数据 (无数据不预测)
            if curr_month in known_months:
                final_price = max(0.0, float(price))
                print(f"{curr_date.strftime('%Y-%m-%d')},{final_price:.2f}")
                has_output = True

        if not has_output:
            return_missing_json("预测时间段内无历史同期数据的参考，停止输出")

        sys.exit(0)

    # ==========================================
    # 模式 2: QUERY_META (查询元数据)
    # ==========================================
    elif args.mode == 'QUERY_META':
        if not args.product: sys.exit(1)

        models_pack = get_product_models(args.product)

        if models_pack:
            specs = set()
            pcats = set()
            for key in models_pack.keys():
                parts = key.split('__')
                if len(parts) >= 1: specs.add(parts[0])
                if len(parts) >= 2: pcats.add(parts[1])

            res = {"specs": sorted(list(specs)), "pcats": sorted(list(pcats))}
            print(json.dumps(res, ensure_ascii=False))
            sys.exit(0)
        else:
            # 尝试从原始数据读
            loaded = load_metadata()
            if loaded and 'data' in loaded and args.product in loaded['data']:
                print(json.dumps({"specs": [], "pcats": []}))
            else:
                print(json.dumps({"specs": [], "pcats": []}))
            sys.exit(0)

    # ==========================================
    # 模式 3: VALIDATE_ALL (校验)
    # ==========================================
    elif args.mode == 'VALIDATE_ALL':
        print(json.dumps({"msg": "Prophet Ready"}, ensure_ascii=False))
        sys.exit(0)


if __name__ == "__main__":
    main()