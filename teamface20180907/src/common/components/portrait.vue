<template>
    <div class="portrait_main">
       <div class="simpName">助手</div>
    </div>
  </template>

<script>
export default {
  props: [],
  data () {
    return {
    }
  },
  mounted () {
  },
  methods: {
    selectedAddress () {
      let address = document.getElementById('searchMap').value
      let lng = document.getElementById('lng').value
      let lat = document.getElementById('lat').value
      let addressObj = {
        value: address,
        lng: lng,
        lat: lat
      }
      this.$bus.emit('sendAddress', addressObj, this.mapId, this.itemIndex)
    }
  }
}
</script>

<style lang="scss">
.portrait_main{
    .simpName{display: block;}
}
</style>
