import resolve from '@rollup/plugin-node-resolve';
import commonjs from '@rollup/plugin-commonjs';
import terser from '@rollup/plugin-terser';
import peerDepsExternal from 'rollup-plugin-peer-deps-external';
import typescript from '@rollup/plugin-typescript';
import replace from 'rollup-plugin-replace';
import postcss from 'rollup-plugin-postcss';
import dts from 'rollup-plugin-dts';
import svgr from '@svgr/rollup';
import sass from 'sass';

export default [
  {
    input: 'src/index.ts',
    output: {
      dir: 'dist',
      format: 'esm',
      sourcemap: true,
    },
    plugins: [
      svgr({
        svgo: false,
        ref: true,
      }),
      peerDepsExternal(),
      resolve(),
      commonjs(),
      typescript({
        tsconfig: './tsconfig.json',
        declaration: true,
        declarationDir: 'dist',
      }),
      postcss({
        modules: true,
        extract: false,
        use: [
          [
            'sass',
            {
              implementation: {
                renderSync: sass.compileString,
              },
            },
          ],
        ],
        extensions: ['.css', '.scss'],
      }),
      // временная замена для игнорирования директив "use client" и устаревших конструкций,
      // необходима из-за несовместимости antd@5.9.0+ с React 16.14.0 и Rollup
      replace({
        'use client': '',
        preventAssignment: true,
      }),
      terser(),
    ],
  },
  {
    input: 'src/index.ts',
    output: [{ file: 'dist/index.d.ts', format: 'esm' }],
    plugins: [dts()],
    external: [/\.(css|less|scss)$/],
  },
];
