import React, { FC } from 'react';
import { AssistantContext, AssistantProps } from '../context/Assistant.context';

const AssistantProvider: FC<{ value: AssistantProps }> = ({ value, children }) => (
  <AssistantContext.Provider value={value}>
    {children}
  </AssistantContext.Provider>
);

export default AssistantProvider;
