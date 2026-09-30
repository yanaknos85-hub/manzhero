import React, { useMemo } from 'react';
import Select from 'antd/lib/select';

import { Agent } from '../../api/ai-assistant/ai-assistant.type';
import Icon from '../../components/Icon/Icon';

import styles from './SelectAssistant.module.scss';

interface SelectAssistantProps {
  value?: string;
  agents: Agent[];
  onChange: (value: string) => void;
}

const SelectAssistant: React.FC<SelectAssistantProps> = ({
  agents, value, onChange,
}) => {
  const options = useMemo(() => (
    agents.map(agent => ({ value: agent.id, label: agent.namePrompt }))
  ), [agents]);

  return (
    <Select
      value={value}
      options={options}
      bordered={false}
      className={styles.select}
      popupClassName={styles.selectDropdown}
      suffixIcon={<Icon name="arrow" />}
      onChange={onChange}
    />
  );
};

export default SelectAssistant;
