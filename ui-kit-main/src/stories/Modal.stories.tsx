import { Meta, StoryFn } from '@storybook/react';
import React, { useState } from 'react';
import {
  Modal, ConfigProvider, IModal, Button
} from '../index';

export default {
  title: 'Elements/Modal',
  component: Modal,
} as Meta<IModal>;

const baseArgs: IModal = {
  title: 'Сбертранспорт',
};

const Template: StoryFn<IModal> = args => {
  const [isModalOpen, setIsModalOpen] = useState(false);

  const showModal = () => {
    setIsModalOpen(true);
  };

  const handleOk = () => {
    setIsModalOpen(false);
  };

  const handleCancel = () => {
    setIsModalOpen(false);
  };

  return (
    <ConfigProvider>
      <Button type="primary" onClick={showModal}>
        Open Modal
      </Button>
      <Modal
        {...args}
        open={isModalOpen}
        onOk={handleOk}
        onCancel={handleCancel}
      >
        <p>Some contents...</p>
        <p>Some contents...</p>
        <p>Some contents...</p>
      </Modal>
    </ConfigProvider>
  );
};

export const Default = Template.bind({});
Default.args = {
  ...baseArgs,
};
