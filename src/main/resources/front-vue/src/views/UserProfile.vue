<template>
  <div class="main-bg">
    <header class="header">
      <h1>个人中心 - 地址管理</h1>
    </header>

    <div class="content">
      <!-- 农户视图 -->
      <div v-if="role === 'farmer'">
        <h2>农户地址管理</h2>
        <p>用于配送农产品、发货地址等。</p>
        <div v-for="a in farmerAddresses" :key="a.addressId" class="address-card">
          <p><strong>{{ a.recipientName }}</strong> - {{ a.phoneNumber }}</p>
          <p>{{ a.province }} {{ a.city }} {{ a.district }} {{ a.streetAddress }}</p>
          <p v-if="a.isDefault">默认地址 ✅</p>
          <button @click="setDefaultAddress(a.addressId)">设为默认</button>
          <button @click="deleteAddress(a.addressId)">删除</button>
        </div>
        <button @click="addAddress">➕ 新增地址</button>
      </div>

      <!-- 买家视图 -->
      <div v-else-if="role === 'customer'">
        <h2>采购商地址管理</h2>
        <p>用于收货、订单结算地址。</p>
        <div v-for="a in customerAddresses" :key="a.addressId" class="address-card">
          <p><strong>{{ a.recipientName }}</strong> - {{ a.phoneNumber }}</p>
          <p>{{ a.province }} {{ a.city }} {{ a.district }} {{ a.streetAddress }}</p>
          <p v-if="a.isDefault">默认地址 ✅</p>
          <button @click="setDefaultAddress(a.addressId)">设为默认</button>
          <button @click="deleteAddress(a.addressId)">删除</button>
        </div>
        <button @click="addAddress">➕ 新增地址</button>
      </div>

      <!-- 管理员视图 -->
      <div v-else-if="role === 'admin'">
        <h2>管理员地址查看</h2>
        <p>管理员可查看所有用户的地址信息。</p>
        <div v-for="user in allUserAddresses" :key="user.userId" class="user-addresses">
          <h3>{{ user.userName }} (ID: {{ user.userId }})</h3>
          <div v-for="a in user.addresses" :key="a.addressId" class="address-card">
            <p><strong>{{ a.recipientName }}</strong> - {{ a.phoneNumber }}</p>
            <p>{{ a.province }} {{ a.city }} {{ a.district }} {{ a.streetAddress }}</p>
            <p v-if="a.isDefault">默认地址 ✅</p>
          </div>
        </div>
      </div>

      <!-- 未知角色 -->
      <div v-else>
        <p>请先登录或检查用户角色。</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "ProfileView",

  methods: {
    addAddress() {
      alert("跳转到新增地址页面或弹窗");
    },
    deleteAddress(id) {
      alert(`删除地址：${id}`);
    },
    setDefaultAddress(id) {
      alert(`设为默认地址：${id}`);
    },
  },
};
</script>

<style scoped>
.main-bg {
  background: #f9fafc;
  min-height: 100vh;
  font-family: "Microsoft YaHei";
  padding: 20px;
}
.header {
  background: #40916c;
  color: white;
  padding: 10px 20px;
  border-radius: 6px;
}
.address-card {
  background: #fff;
  border-radius: 8px;
  padding: 10px 15px;
  margin-bottom: 10px;
  box-shadow: 0 0 3px rgba(0,0,0,0.1);
}
.address-card button {
  margin-right: 10px;
  background: #95d5b2;
  border: none;
  padding: 5px 10px;
  cursor: pointer;
  border-radius: 4px;
}
.address-card button:hover {
  background: #74c69d;
}
.user-addresses {
  margin-bottom: 20px;
  background: #edf6f9;
  padding: 10px;
  border-radius: 8px;
}
</style>
