import React, {
  useState, useEffect, useMemo, useRef
} from 'react';
import { Modal } from 'antd';

import { useAgents, useAssistantMessage, useAssessment } from '../../api/ai-assistant/ai-assistant.api';
import { Agent, Assessment } from '../../api/ai-assistant/ai-assistant.type';
import {
  AGENT_ID_KEY, AI_LOGO_POSITION_KEY, AIType, defaultSetting, EvaluationTypes
} from '../../constants/constants';

import Footer from '../../components/Footer/Footer';
import AiLogo from '../../components/AiLogo/AiLogo';
import Conversation from '../../components/Conversation/Conversation';
import DislikeModal from '../../components/DislikeModal/DislikeModal';
import SelectAssistant from '../../components/SelectAssistant/SelectAssistant';
import Icon from '../../components/Icon/Icon';

import useDraggable from '../../hooks/useDraggable';
import useChatHistory from '../../hooks/useChatHistory';
import useAgent from '../../hooks/useAgent';

import AssistantProvider from '../../context/AssistantProvider';
import { AssistantProps, useAssistantContext } from '../../context/Assistant.context';
import { ChatHistory } from '../../types/types';
import { ignore } from '../../utils';
import { UUID } from '../../utils/io-ts';

import styles from './AiAssistant.module.scss';

const AiAssistant = () => {
  const { agentType, userId } = useAssistantContext();

  const getAgents = useAgents();
  const onAssistantMessage = useAssistantMessage();
  const onAssessment = useAssessment();

  const [agents, setAgents] = useState<Agent[]>([]);
  const [isLoading, setLoading] = useState(false);
  const [visible, setVisible] = useState(false);
  const [dialog, setDialog] = useState<ChatHistory | null>(null);

  const { activeAgent, onActiveAgent } = useAgent(agents);

  const contentRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    getAgents(AIType[agentType]).then(setAgents);
  }, [agentType, getAgents]);

  const {
    chatHistory, onAddHistory, onEditHistory, onResetChat,
  } = useChatHistory();

  const {
    position, hasMoved, handleMouseDown,
  } = useDraggable({
    localKey: AI_LOGO_POSITION_KEY,
    itemSize: 70,
  });

  const namePrompt = useMemo(() => (
    activeAgent?.namePrompt ?? defaultSetting.namePrompt
  ), [activeAgent]);

  const onVisible = () => {
    !hasMoved && setVisible(true);
  };

  const onMessage = (question: string) => {
    const historyId = onAddHistory(question);

    setTimeout(() => {
      if (contentRef.current) {
        contentRef.current.scrollTop = contentRef.current.scrollHeight;
      }
    }, 0);

    setLoading(true);

    onAssistantMessage({ question, agentId: activeAgent!.id })
      .then(data => {
        if (data?.answer) {
          onEditHistory(historyId, { answer: data.answer });
        }
      })
      .catch(ignore)
      .finally(() => setLoading(false));
  };

  const handleAssessment = (dialogId: UUID, assessment: Assessment) => {
    if (assessment.userEvaluation === EvaluationTypes.DISLIKE && !assessment.comment) {
      return setDialog({
        id: dialogId,
        question: assessment.question,
        answer: assessment.answer,
        userEvaluation: assessment.userEvaluation,
      });
    }

    return onAssessment({ ...assessment, userId: userId })
      .then(() => {
        onEditHistory(dialogId, {
          userEvaluation: assessment.userEvaluation,
          comment: assessment.comment,
        });
      })
      .catch(ignore);
  };

  const onChangeAgent = (agentId: string) => {
    localStorage.setItem(AGENT_ID_KEY, agentId);
    onActiveAgent(agentId);
    onResetChat();
  };

  return (
    <>
      {!visible && !!activeAgent && (
        <AiLogo
          animation
          position={position ? { left: position.x, top: position.y } : { bottom: 20, right: 20 }}
          onClick={onVisible}
          onMouseDown={handleMouseDown}
        />
      )}
      <Modal
        open={visible}
        centered={false}
        className={styles.modal}
        wrapClassName={styles.wrapper}
        width={480}
        closeIcon={<Icon name="close" />}
        title={(
          <>
            <Icon name="agent" />
            <SelectAssistant
              agents={agents}
              value={activeAgent?.id}
              onChange={onChangeAgent}
            />
          </>
        )}
        onCancel={() => setVisible(false)}
        footer={<Footer isDisabled={isLoading} onMessage={onMessage} />}
      >
        <div ref={contentRef} className={styles.content}>
          <Conversation
            chatHistory={chatHistory}
            namePrompt={namePrompt}
            isLoading={isLoading}
            onAssessment={handleAssessment}
            onMessage={onMessage}
          />
        </div>
      </Modal>
      <DislikeModal
        dialog={dialog}
        onAssessment={handleAssessment}
        onClose={() => setDialog(null)}
      />
    </>
  );
};

export default (props: AssistantProps) => (
  <AssistantProvider value={props}>
    <AiAssistant />
  </AssistantProvider>
);
