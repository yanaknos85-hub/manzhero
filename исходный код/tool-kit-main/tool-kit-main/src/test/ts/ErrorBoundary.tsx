import React, { Component, FC, ReactNode } from 'react';

type ErrorBoundaryProps = {
  fallback?: ReactNode;
  // children: ReactNode;
};

type ErrorBoundaryState = {
  error: Error | null;
};

const DefaultFallback: FC<{ error: Error | null }> = () => (
  <div>
    Что-то пошло не так!
  </div>
);

export default class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);

    this.state = {
      error: null,
    };
  }

  get fallback(): ReactNode {
    const { fallback } = this.props;
    const { error } = this.state;
    return fallback || <DefaultFallback error={error} />;
  }

  clearErrors(): void {
    this.setState({
      error: null,
    });
  }

  render(): ReactNode {
    const { error } = this.state;
    const { children } = this.props;
    return error ? this.fallback : children;
  }
}
