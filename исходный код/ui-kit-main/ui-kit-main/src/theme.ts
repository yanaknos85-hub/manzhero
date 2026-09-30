import { ThemeConfig } from 'antd/lib/config-provider';
import { Button } from './components/Button/Button.theme';
import { Checkbox } from './components/Checkbox/Checkbox.theme';
import { Input } from './components/Input/Input.theme';
import { Radio } from './components/Radio/Radio.theme';
import { Select } from './components/Select/Select.theme';
import { Spin } from './components/Spin/Spin.theme';
import { Switch } from './components/Switch/Switch.theme';
import { Tabs } from './components/Tabs/Tabs.theme';
import { Modal } from './components/Modal/Modal.theme';
import { Typography } from './components/Typography/Typography.theme';
import { Pagination } from './components/Pagination/Pagination.theme';
import { Table } from './components/Table/Table.theme';
import { Color, FontFamily, FontSize } from './constants.theme';

const theme: ThemeConfig = {
  token: {
    fontFamily: FontFamily.default,
    fontSize: FontSize.default,
    colorText: Color.text,
    colorLink: Color.text,
    colorLinkHover: Color.green,
    borderRadius: 8,
    borderRadiusSM: 8,
    borderRadiusLG: 8,
    lineWidth: 1, // border width
    controlOutlineWidth: 1,
    controlOutline: Color.green,
    controlHeightXS: 40,
    controlHeight: 48,
    controlHeightLG: 56,
  },
  components: {
    Input,
    Button,
    Select,
    Spin,
    Tabs,
    Checkbox,
    Radio,
    Switch,
    Modal,
    Typography,
    Pagination,
    Table,
  },
};

export default theme;
