import React from 'react';
interface QuestionsProps {
    questions: string[];
    onMessage: (message: string) => void;
}
declare const Questions: React.FC<QuestionsProps>;
export default Questions;
