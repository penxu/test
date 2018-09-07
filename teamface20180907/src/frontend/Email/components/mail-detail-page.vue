<template>
  <div class="mail-detail-page">
    <MailContentParticulars v-if="currentId != ''" :id="currentId"></MailContentParticulars>
  </div>
</template>
<script>
import MailContentParticulars from '@/frontend/Email/components/Email-components-particulars' // 邮件详情组件
export default {
  name: 'mail-detail-page', // 邮件详情跳页组件
  components: {MailContentParticulars},
  data () {
    return {
      currentId: ''
    }
  },
  mounted () {
    this.currentId = this.$router.history.current.query.id
  }
}
</script>
<style lang="scss">
.mail-detail-page {
  padding: 15px;
  height: 100%;
  overflow-y: auto;
}
</style>
