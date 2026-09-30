/** Типы оценок */
export declare enum EvaluationTypes {
    LIKE = "LIKE",
    DISLIKE = "DISLIKE",
    NOT_EVALUATED = "NOT_EVALUATED"
}
export declare enum AIType {
    WebCorp = "WEB_CORP",
    Client = "CLIENT",
    Dispatcher = "DISPATCHER",
    Autoservice = "AUTOSERVICE"
}
export declare const defaultSetting: {
    namePrompt: string;
};
export declare const DISLIKE_OPTIONS: string[];
export declare const initialQuestions: never[];
export declare const AI_LOGO_POSITION_KEY = "aiLogoPosition";
export declare const AGENT_ID_KEY = "activeAgentId";
export declare const months: string[];
