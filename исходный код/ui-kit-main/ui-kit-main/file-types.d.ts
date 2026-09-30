/// <reference types="react-scripts" />

declare module '*.jpg' {
  const content: string;
  export default content;
}

declare module '*.module.scss' {
  const content: Record<string, string>;
  export default content;
}

declare module '*.less' {
  const content: Record<string, string>;
  export default content;
}
