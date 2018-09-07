<template>
  <layout-text v-if="property.type === 'text'" :property="property" :saveData="saveData" :LinkageFields="LinkageFields" :projectOrPersonalTask="projectOrPersonalTask"></layout-text>
  <layout-textarea v-else-if="property.type === 'textarea'" :property="property" :saveData="saveData"></layout-textarea>
  <layout-picklist v-else-if="property.type === 'picklist'" :property="property" :saveData="saveData" :LinkageFields="LinkageFields" :layout="layout" :dataId="dataId"></layout-picklist>
  <layout-phone v-else-if="property.type === 'phone'" :property="property" :saveData="saveData" :LinkageFields="LinkageFields" :projectOrPersonalTask="projectOrPersonalTask"></layout-phone>
  <layout-email v-else-if="property.type === 'email'" :property="property" :saveData="saveData" :LinkageFields="LinkageFields" :projectOrPersonalTask="projectOrPersonalTask"></layout-email>
  <layout-number v-else-if="property.type === 'number'" :property="property" :saveData="saveData" :LinkageFields="LinkageFields"></layout-number>
  <layout-datetime v-else-if="property.type === 'datetime'" :property="property" :saveData="saveData" :LinkageFields="LinkageFields"></layout-datetime>
  <layout-attachment v-else-if="property.type === 'attachment'" :bean="bean" :property="property" :saveData="saveData" :onTrial="onTrial"></layout-attachment>
  <layout-url v-else-if="property.type === 'url'" :property="property" :saveData="saveData" :LinkageFields="LinkageFields"></layout-url>
  <layout-location v-else-if="property.type === 'location'" :property="property" :saveData="saveData" :LinkageFields="LinkageFields"></layout-location>
  <layout-picture v-else-if="property.type === 'picture'" :bean="bean" :property="property" :saveData="saveData" :onTrial="onTrial"></layout-picture>
  <layout-multi v-else-if="property.type === 'multi'" :property="property" :saveData="saveData" :LinkageFields="LinkageFields"></layout-multi>
  <layout-multipicklist v-else-if="property.type === 'mutlipicklist'" :property="property" :saveData="saveData"></layout-multipicklist>
  <layout-personnel v-else-if="property.type === 'personnel'" :property="property" :saveData="saveData" :LinkageFields="LinkageFields"></layout-personnel>
  <layout-department v-else-if="property.type === 'department'" :property="property" :saveData="saveData" :LinkageFields="LinkageFields"></layout-department>
  <layout-area v-else-if="property.type === 'area'" :property="property" :saveData="saveData" :LinkageFields="LinkageFields"></layout-area>
  <layout-reference v-else-if="property.type === 'reference'" :property="property" :saveData="saveData" :onTrial="onTrial" :LinkageFields="LinkageFields"></layout-reference>
  <layout-uediter v-else-if="property.type === 'multitext'" :property="property" :saveData="saveData" :onTrial="onTrial"></layout-uediter>
  <layout-barcode v-else-if="property.type === 'barcode'" :property="property" :saveData="saveData" :LinkageFields="LinkageFields"></layout-barcode>
  <layout-subform v-else-if="property.type === 'subform'" :property="property" :saveData="saveData" :dataId="dataId" :onTrial="onTrial" :LinkageFields="LinkageFields" :projectOrPersonalTask="projectOrPersonalTask"></layout-subform>
</template>

<script>
import LayoutText from '@/common/layout/layout-text'
import LayoutTextarea from '@/common/layout/layout-textarea'
import LayoutPicklist from '@/common/layout/layout-picklist'
import LayoutPhone from '@/common/layout/layout-phone'
import LayoutEmail from '@/common/layout/layout-email'
import LayoutNumber from '@/common/layout/layout-number'
import LayoutDatetime from '@/common/layout/layout-datetime'
import LayoutAttachment from '@/common/layout/layout-attachment'
import LayoutUrl from '@/common/layout/layout-url'
import LayoutLocation from '@/common/layout/layout-location'
import LayoutPicture from '@/common/layout/layout-picture'
import LayoutMulti from '@/common/layout/layout-multi'
import LayoutMultipicklist from '@/common/layout/layout-multipicklist'
import LayoutPersonnel from '@/common/layout/layout-personnel'
import LayoutDepartment from '@/common/layout/layout-department'
import LayoutArea from '@/common/layout/layout-area'
import LayoutReference from '@/common/layout/layout-reference'
import LayoutSubform from '@/common/layout/layout-subform'
import LayoutUediter from '@/common/layout/layout-uediter' // 富文本
import LayoutBarcode from '@/common/layout/layout-barcode'
export default {
  name: 'LayoutForm',
  components: {
    LayoutText,
    LayoutTextarea,
    LayoutPicklist,
    LayoutPhone,
    LayoutEmail,
    LayoutNumber,
    LayoutDatetime,
    LayoutAttachment,
    LayoutUrl,
    LayoutLocation,
    LayoutPicture,
    LayoutMulti,
    LayoutMultipicklist,
    LayoutPersonnel,
    LayoutDepartment,
    LayoutArea,
    LayoutReference,
    LayoutSubform,
    LayoutUediter,
    LayoutBarcode
  },
  props: ['bean', 'property', 'saveData', 'dataId', 'layout', 'onTrial', 'LinkageFields', 'projectOrPersonalTask'],
  data () {
    return {
    }
  }
}
</script>

<style lang="scss">
@import '../scss/component.scss';
</style>
