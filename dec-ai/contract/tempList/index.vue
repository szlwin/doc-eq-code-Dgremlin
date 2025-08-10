<template>
  <div class="contract-template">
    <tables
      :filter-list="filterList"
      :columns="columns"
      :params-rewrite="paramsRewrite"
      :query-params="{ type: 2 }"
      api-name="contractAdminTemplateQueryByPage"
    >
      <template #attrUrl="row">
        <a class="clr-blue" target="_blank" @click="look(row)"> 查看附件 </a>
      </template>
    </tables>
  </div>
</template>

<script>
import { filterList, columns } from './constant';
import privateImg from '@/assets/js/mixins/privateImg';

export default {
  name: 'ContractTemplate',
  mixins: [privateImg],
  data() {
    return {
      filterList,
      columns
    };
  },
  methods: {
    paramsRewrite(params) {
      for (let k in params) {
        if (!params[k]) {
          params[k] = undefined;
        }
      }
      return params;
    },
    look(row) {
      this.showPrivateImg(row.attrUrl, window.open);
    }
  }
};
</script>

<style lang="scss" scoped>
.contract-template {
  .clr-blue {
    color: $blue;
  }
}
</style>
