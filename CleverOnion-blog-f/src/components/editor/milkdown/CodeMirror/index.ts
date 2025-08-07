import type { LanguageDescription } from '@codemirror/language'
import type { Extension } from '@codemirror/state'

import { defaultKeymap, indentWithTab } from '@codemirror/commands'
import { keymap } from '@codemirror/view'
import {
  codeBlockComponent,
  codeBlockConfig,
} from '@milkdown/kit/component/code-block'
import { basicSetup } from 'codemirror'
import { Ctx } from '@milkdown/kit/ctx'

// 图标定义
const chevronDownIcon = '▼'
const clearIcon = '✕'
const copyIcon = '📋'
const editIcon = '✏️'
const searchIcon = '🔍'
const visibilityOffIcon = '👁️'

interface CodeMirrorConfig {
  extensions: Extension[]
  languages: LanguageDescription[]
  theme: Extension

  expandIcon: string
  searchIcon: string
  clearSearchIcon: string

  searchPlaceholder: string
  copyText: string
  copyIcon: string
  onCopy: (text: string) => void
  noResultText: string

  renderLanguage: (language: string, selected: boolean) => string

  renderPreview: (
    language: string,
    content: string
  ) => string | HTMLElement | null

  previewToggleIcon: (previewOnlyMode: boolean) => string
  previewToggleText: (previewOnlyMode: boolean) => string
  previewLabel: string
}

export type CodeMirrorFeatureConfig = Partial<CodeMirrorConfig>

export const configureCodeMirror = (
  ctx: Ctx,
  config: CodeMirrorFeatureConfig = {}
) => {
  const { languages = [], theme } = config
  const extensions = [
    keymap.of(defaultKeymap.concat(indentWithTab)),
    basicSetup,
  ]
  
  if (theme) {
    extensions.push(theme)
  }
  
  if (config.extensions) {
    extensions.push(...config.extensions)
  }

  ctx.update(codeBlockConfig.key, (defaultConfig) => ({
    extensions,
    languages,

    expandIcon: config.expandIcon || chevronDownIcon,
    searchIcon: config.searchIcon || searchIcon,
    clearSearchIcon: config.clearSearchIcon || clearIcon,
    searchPlaceholder: config.searchPlaceholder || 'Search language',
    copyText: config.copyText || 'Copy',
    copyIcon: config.copyIcon || copyIcon,
    onCopy: config.onCopy || ((text: string) => {
      navigator.clipboard.writeText(text).catch(console.error)
    }),
    noResultText: config.noResultText || 'No result',
    renderLanguage: config.renderLanguage || defaultConfig.renderLanguage,
    renderPreview: config.renderPreview || defaultConfig.renderPreview,
    previewToggleButton: (previewOnlyMode: boolean) => {
      const icon =
        config.previewToggleIcon?.(previewOnlyMode) ||
        (previewOnlyMode ? editIcon : visibilityOffIcon)
      const text =
        config.previewToggleText?.(previewOnlyMode) ||
        (previewOnlyMode ? 'Edit' : 'Hide')
      return [icon, text].map((v) => v.trim()).join(' ')
    },
    previewLabel: config.previewLabel || defaultConfig.previewLabel,
  }))
}

export { codeBlockComponent }