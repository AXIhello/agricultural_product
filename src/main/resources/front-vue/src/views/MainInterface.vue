<template>
  <div class="main-bg">
    <HeaderComponent />
    <section class="content">
      <div class="home-content">
        <!--新闻轮播展示-->
        <div class="news-carousel">
          <h3>最新资讯</h3>
           <NewsCarousel />
        </div>

        <!--金融产品展示-->
        <div class="financial-products">
          <h3>金融产品</h3>
          <FinanceProducts />
        </div>

        <!-- 专家一览 -->
        <div class="expert-overview">
          <h3>专家推荐</h3>
          <ExpertOverview />
        </div>

        <!-- 专家问答展示 -->
        <div class="expert-qa">
          <h3>专家问答</h3>
          <ExpertQA />
        </div>

        <!-- 助农电商 -->
        <div class="e-commerce">
          <h3>助农电商</h3>
          <ECommerce />
        </div>

      </div>

      <router-view></router-view>
    </section>
  </div>
</template>

<script setup>
import NewsCarousel from '../components/NewsCarousel.vue';
import FinanceProducts from '../components/FinanceProducts.vue';
import ExpertOverview from '../components/ExpertOverview.vue';
import ExpertQA from '../components/ExpertQA.vue';
import ECommerce from '../components/ECommerce.vue';
import HeaderComponent from '../components/HeaderComponent.vue';

</script>

<style scoped>
.content {
  margin-left: 20px;
  margin-right: 20px;
  width: calc(100% - 40px);
  padding: 26px;
}

.news-carousel,
.finance-products,
.expert-overview,
.expert-qa,
.e-commerce {
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  background-color: #f9f9f9;
}

.home-content h3 {
  color: #2D7D4F; /* 深绿色标题 */
  font-size: 1.2rem;
  margin-bottom: 10px;
}
</style>
