import axios from '@/utils/axios'

/**
 * 智能推荐
 * @param {number} pageNum
 * @param {number} pageSize
 * @param {string} category 可选
 */
export const getRecommend = (pageNum = 1, pageSize = 10, category = '') =>
  axios.get('/products/recommend', { params: { pageNum, pageSize, category } })

/**
 * 热销榜
 */
export const getHotProducts = (pageNum = 1, pageSize = 10) =>
  axios.get('/products/hot', { params: { pageNum, pageSize } })

/**
 * 高评分榜
 */
export const getHighRated = (pageNum = 1, pageSize = 10, minRatingCount = 5) =>
  axios.get('/products/high-rated', { params: { pageNum, pageSize, minRatingCount } })