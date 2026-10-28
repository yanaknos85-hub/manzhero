import type { Preview } from "@storybook/react";

// Global Styles!
import '../static/global.css';
import '../static/sb-sans.css';

const preview: Preview = {
  parameters: {
    actions: { argTypesRegex: "^on[A-Z].*" },
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/,
      },
    },
  },
};

export default preview;
