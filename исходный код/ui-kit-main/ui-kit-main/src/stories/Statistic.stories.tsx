import { Meta, StoryFn } from '@storybook/react';
import React from 'react';
import {
  ConfigProvider, IStatistic, Statistic
} from '../index';
import { Col, Row } from 'antd';
import { LikeOutlined } from '@ant-design/icons';

export default {
  title: 'Elements/Statistic',
  component: Statistic,
} as Meta<IStatistic>;

const Template: StoryFn<IStatistic> = args => (
  <ConfigProvider>
    <Row gutter={10}>
      <Col span={11}>
        <Statistic {...args} />
      </Col>
    </Row>
  </ConfigProvider>
);

const baseArgs: IStatistic = {
  title: 'Default',
  value: 'Statistic Component\'s value',
};

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
  valueStyle: { color: '#10BF6A' },
};

export const Loading = Template.bind({});
Loading.args = {
  ...baseArgs,
  title: 'Loading',
  value: 45454,
  loading: true,
};

export const Prefix = Template.bind({});
Prefix.args = {
  ...baseArgs,
  title: 'Prefix',
  value: 1000,
  prefix: <LikeOutlined />,
};

export const Suffix = Template.bind({});
Suffix.args = {
  ...baseArgs,
  title: 'Suffix',
  value: 80,
  suffix: '/ 100',
};
