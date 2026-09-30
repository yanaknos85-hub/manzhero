import React, { Suspense } from 'react';
export var CustomSuspense = function (_a) {
    var children = _a.children, fallback = _a.fallback;
    return (React.createElement(Suspense, { fallback: fallback }, children));
};
