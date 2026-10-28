import React from 'react';
import { Agent } from '../../api/ai-assistant/ai-assistant.type';
interface SelectAssistantProps {
    value?: string;
    agents: Agent[];
    onChange: (value: string) => void;
}
declare const SelectAssistant: React.FC<SelectAssistantProps>;
export default SelectAssistant;
