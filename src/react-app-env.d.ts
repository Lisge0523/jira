/// <reference types="react-scripts" />

declare module '*.css' {
  const content: Record<string, string>;
  export default content;
}

declare module '*.less' {
  const content: any;
  export default content;
}
