export const columns = [
  {
    type: 'index',
    label: '序号',
    width: '80',
    align: 'center'
  },
  {
    label: '模板编号',
    prop: 'templateCode',
    align: 'center'
  },
  {
    label: '模板名称',
    prop: 'name',
    align: 'center'
  },
  {
    label: '模板附件',
    prop: 'attrUrl',
    align: 'center',
    width: 100
  }
];

export const filterList = [
  {
    type: 'input',
    label: '模板编号:',
    placeholder: '请输入',
    prop: 'templateCode',
    templateCode: '',
    disabled: false,
    clearable: true
  },
  {
    type: 'input',
    label: '模板名称:',
    placeholder: '请输入',
    prop: 'name',
    name: '',
    disabled: false,
    clearable: true
  }
];
