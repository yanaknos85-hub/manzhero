import { useEffect, useState } from 'react';

import { Agent } from '../api/ai-assistant/ai-assistant.type';
import { AGENT_ID_KEY } from '../constants/constants';

const savedAgentId = localStorage.getItem(AGENT_ID_KEY) ?? undefined;

const useAgent = (agents: Agent[]) => {
  const [activeAgent, setActiveAgent] = useState<Agent>();

  useEffect(() => {
    if (agents?.length) {
      const savedAgent = savedAgentId ? agents.find(agent => agent.id === savedAgentId) : null;

      setActiveAgent(savedAgent ? savedAgent : agents[0]);
    }
  }, [agents]);

  const onActiveAgent = (agentId: string) => {
    setActiveAgent(agents.find(({ id }) => id === agentId));
  };

  return {
    activeAgent,
    onActiveAgent,
  };
};

export default useAgent;
