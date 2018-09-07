<template>
  <layout-text v-if="property.type === 'text'" :property="property" :saveData="saveData"></layout-text>
  <layout-textarea v-else-if="property.type === 'textarea'" :property="property" :saveData="saveData"></layout-textarea>
  <layout-picklist-app v-else-if="property.type === 'picklist'" :property="property" :saveData="saveData"></layout-picklist-app>
  <layout-phone v-else-if="property.type === 'phone'" :property="property" :saveData="saveData"></layout-phone>
  <layout-email v-else-if="property.type === 'email'" :property="property" :saveData="saveData"></layout-email>
  <layout-number v-else-if="property.type === 'number'" :property="property" :saveData="saveData"></layout-number>
  <layout-datetime v-else-if="property.type === 'datetime'" :property="property" :saveData="saveData"></layout-datetime>
  <layout-attachment-app v-else-if="property.type === 'attachment'" :bean="bean" :property="property" :saveData="saveData"></layout-attachment-app>
  <layout-url v-else-if="property.type === 'url'" :property="property" :saveData="saveData"></layout-url>
  <layout-location v-else-if="property.type === 'location'" :property="property" :saveData="saveData"></layout-location>
  <layout-picture-app v-else-if="property.type === 'picture'" :bean="bean" :property="property" :saveData="saveData"></layout-picture-app>
  <layout-multi-app v-else-if="property.type === 'multi'" :property="property" :saveData="saveData"></layout-multi-app>
  <layout-multipicklist-app v-else-if="property.type === 'mutlipicklist'" :property="property" :saveData="saveData"></layout-multipicklist-app>
  <layout-personnel v-else-if="property.type === 'personnel'" :property="property" :saveData="saveData"></layout-personnel>
  <layout-department v-else-if="property.type === 'department'" :property="property" :saveData="saveData" ></layout-department>
  <layout-area-app v-else-if="property.type === 'area'" :property="property" :saveData="saveData"></layout-area-app>
  <layout-barcode-app v-else-if="property.type === 'barcode'" :property="property" :saveData="saveData"></layout-barcode-app>
  <layout-reference v-else-if="property.type === 'reference'" :property="property" :saveData="saveData"></layout-reference>
  <layout-subform-app v-else-if="property.type === 'subform'" :property="property" :saveData="saveData" :dataId="dataId"></layout-subform-app>
  <layout-uediter :property="property" :saveData="saveData" v-else-if="property.type === 'multitext'"></layout-uediter>
</template>

<script>
import LayoutText from '@/common/layout/layout-text'
import LayoutTextarea from '@/common/layout/layout-textarea'
import LayoutPicklistApp from '@/common/layout/layout-picklist-app'
import LayoutPhone from '@/common/layout/layout-phone'
import LayoutEmail from '@/common/layout/layout-email'
import LayoutNumber from '@/common/layout/layout-number'
import LayoutDatetime from '@/common/layout/layout-datetime'
import LayoutAttachmentApp from '@/common/layout/layout-attachment-app'
import LayoutUrl from '@/common/layout/layout-url'
import LayoutLocation from '@/common/layout/layout-location'
import LayoutPictureApp from '@/common/layout/layout-picture-app'
import LayoutMultiApp from '@/common/layout/layout-multi-app'
import LayoutMultipicklistApp from '@/common/layout/layout-multipicklist-app'
import LayoutPersonnel from '@/common/layout/layout-personnel'
import LayoutDepartment from '@/common/layout/layout-department'
import LayoutAreaApp from '@/common/layout/layout-area-app'
import LayoutReference from '@/common/layout/layout-reference'
import LayoutBarcodeApp from '@/common/layout/layout-barcode-app'
import LayoutSubformApp from '@/common/layout/layout-subform-app'
import LayoutUediter from '@/common/layout/layout-uediter' // 富文本
export default {
  name: 'LayoutForm',
  components: {
    LayoutText,
    LayoutTextarea,
    LayoutPicklistApp,
    LayoutPhone,
    LayoutEmail,
    LayoutNumber,
    LayoutDatetime,
    LayoutAttachmentApp,
    LayoutUrl,
    LayoutLocation,
    LayoutPictureApp,
    LayoutMultiApp,
    LayoutMultipicklistApp,
    LayoutPersonnel,
    LayoutDepartment,
    LayoutAreaApp,
    LayoutBarcodeApp,
    LayoutReference,
    LayoutSubformApp,
    LayoutUediter
  },
  props: ['bean', 'property', 'saveData', 'dataId'],
  data () {
    return {
    }
  }
}
</script>

<style lang="scss">
@import '../scss/component-app.scss';
</style>
