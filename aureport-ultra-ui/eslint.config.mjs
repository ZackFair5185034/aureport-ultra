import antfu from '@antfu/eslint-config'

export default antfu(
  {
    // 忽略不需要 lint 检查的文件和目录
    ignores: [
      'node_modules',
      '**/node_modules/**',
      'dist',
      '**/dist/**',
      'public',
      '**/public/**',
      'src/assets',
      '**/src/assets/**',
      '.git',
      '.idea',
      '.vscode',
      '*.config.*',
    ],
    rules: {
      // 允许文件名使用任意大小写（如 index.vue, MyComponent.vue）
      'unicorn/filename-case': 'off',
      // 允许使用常见的简写变量名（如 props, e, err）
      'unicorn/prevent-abbreviations': 'off',
      // 允许空文件（避免单行文件报错）
      'unicorn/no-empty-file': 'off',
      // 允许使用 console（方便调试）
      'no-console': 'off',
      // 关闭 ===/!== 强制检查（避免 == null 等常见模式报错）
      'eqeqeq': 'off',
      'vue/eqeqeq': 'off',
      // 关闭单行 HTML 元素内容换行检查
      'vue/singleline-html-element-content-newline': 'off',
      // 允许 v-on 事件名使用下划线（如 v-on:click_native）
      'vue/v-on-event-hyphenation': 'off',
      // 允许属性名使用下划线（如 data_id）
      'vue/attribute-hyphenation': 'off',
      // 允许 v-for 和 v-if 共用同一元素（性能警告可忽略）
      'vue/no-use-v-if-with-v-for': 'off',
      // 允许自定义事件名使用任意风格（Vue3 推荐 camelCase，如 update:modelValue）
      'vue/custom-event-name-casing': 'off',
    },
  },
  {
    // 适用于所有 Vue/TS/JS 文件的规则配置
    files: ['**/*.vue', '**/*.ts', '**/*.js', '**/*.tsx', '**/*.jsx'],
    rules: {
      // 允许未使用的导入（方便保留导入供后续使用）
      'unused-imports/no-unused-vars': 'off',
      'unused-imports/no-unused-imports': 'off',
      // 允许使用全局 process（Node.js 环境中常用）
      'node/prefer-global/process': 'off',
      // 允许在定义之前使用类型（避免 TypeScript 声明顺序问题）
      'ts/no-use-before-define': 'off',
      // 允许空代码块（避免 if/else 空块报错）
      'no-empty': 'off',
      // 允许未使用的捕获组（正则表达式中常见）
      'regexp/no-unused-capturing-group': 'off',
      // 允许使用 array.includes 代替 set.has（有时代码更直观）
      'unicorn/prefer-set-has': 'off',
      // 允许使用 array.reduce（功能强大，误用风险可控）
      'unicorn/no-array-reduce': 'off',
      // 允许使用 null（某些场景 null 比 undefined 更语义化）
      'unicorn/no-null': 'off',
      // 允许 switch case 不加大括号（风格偏好）
      'unicorn/switch-case-braces': 'off',
      // 允许数字字面量使用下划线分隔（长数字可读性）
      'unicorn/numeric-separators-style': 'off',
      // 允许数组方法回调中不传 this 参数
      'unicorn/no-array-callback-reference': 'off',
      // 允许数组方法 this 参数使用 thisArg
      'unicorn/no-array-method-this-argument': 'off',
      // 允许使用 Number 全局属性（如 Number.NaN）
      'unicorn/prefer-number-properties': 'off',
      // 允许使用 globalThis（跨环境兼容）
      'unicorn/prefer-global-this': 'off',
      // 允许使用 top-level await（ES 模块中常用）
      'unicorn/prefer-top-level-await': 'off',
      // 允许显式长度检查（如 length === 0）
      'unicorn/explicit-length-check': 'off',
      // 允许使用展开运算符而非 Function.prototype.apply
      'unicorn/prefer-spread': 'off',
      // 允许使用 any 类型（过渡时期方便）
      'ts/no-explicit-any': 'off',
      // 允许使用非空断言（!）（如 dom!.click()）
      'ts/no-non-null-assertion': 'off',
      // .d.ts 类型声明文件中 method-signature-style 规则不适用
      'ts/method-signature-style': 'off',
      // 允许未使用的导入（方便保留导入供后续使用）
      'unused-imports/no-unused-vars': 'off',
      'unused-imports/no-unused-imports': 'off',
    },
  },
  {
    // 仅适用于 Vue 单文件组件的规则配置
    files: ['**/*.vue'],
    rules: {
      // HTML 自闭合标签风格：void 元素（如 img, br）必须自闭合，组件必须自闭合
      'vue/html-self-closing': [
        'error',
        {
          html: {
            void: 'always',
            component: 'always',
          },
        },
      ],
      // 多行 HTML 元素内容换行：内容超过一行时必须换行，允许空行，pre/textarea 除外
      'vue/multiline-html-element-content-newline': [
        'error',
        {
          ignoreWhenEmpty: true,
          ignores: ['pre', 'textarea'],
          allowEmptyLines: false,
        },
      ],
      // v-for 必须配合 :key 使用（列表渲染最佳实践）
      'vue/require-v-for-key': 'error',
      // 禁止直接修改 props（ Props 应是不可变的，warn 级别）
      'vue/no-mutating-props': 'warn',
    },
  },
)