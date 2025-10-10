import axios from '../utils/axios'

/**
 * 邮件相关的API服务
 */
export const emailApi = {
    /**
     * 发送验证码
     * @param {string} email - 邮箱地址
     * @returns {Promise} - 请求Promise
     */
    sendVerificationCode(email) {
        return axios.get('/email/send-code', {
            params: { email }
        })
    },

    /**
     * 验证验证码
     * @param {string} email - 邮箱地址
     * @param {string} code - 验证码
     * @returns {Promise} - 请求Promise
     */
    verifyCode(email, code) {
        return axios.post('/email/verify-code', null, {
            params: {
                email,
                code
            }
        })
    }
}