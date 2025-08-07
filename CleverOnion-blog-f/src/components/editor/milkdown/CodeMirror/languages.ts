import { LanguageDescription } from '@codemirror/language'
import { javascript } from '@codemirror/lang-javascript'
import { python } from '@codemirror/lang-python'
import { java } from '@codemirror/lang-java'
import { cpp } from '@codemirror/lang-cpp'
import { css } from '@codemirror/lang-css'
import { html } from '@codemirror/lang-html'
import { json } from '@codemirror/lang-json'
import { markdown } from '@codemirror/lang-markdown'
import { sql } from '@codemirror/lang-sql'
import { xml } from '@codemirror/lang-xml'

/**
 * CodeMirror 支持的编程语言配置
 * 每个语言描述包含名称、别名、扩展名和对应的语言支持
 */
export const codeMirrorLanguages: LanguageDescription[] = [
  LanguageDescription.of({
    name: 'JavaScript',
    alias: ['js', 'javascript', 'jsx'],
    extensions: ['js', 'jsx', 'mjs'],
    load: () => Promise.resolve(javascript({ jsx: true }))
  }),
  
  LanguageDescription.of({
    name: 'TypeScript',
    alias: ['ts', 'typescript', 'tsx'],
    extensions: ['ts', 'tsx'],
    load: () => Promise.resolve(javascript({ typescript: true, jsx: true }))
  }),
  
  LanguageDescription.of({
    name: 'Python',
    alias: ['py', 'python'],
    extensions: ['py', 'pyw'],
    load: () => Promise.resolve(python())
  }),
  
  LanguageDescription.of({
    name: 'Java',
    alias: ['java'],
    extensions: ['java'],
    load: () => Promise.resolve(java())
  }),
  
  LanguageDescription.of({
    name: 'C++',
    alias: ['cpp', 'c++', 'cxx', 'cc', 'c'],
    extensions: ['cpp', 'cxx', 'cc', 'c', 'h', 'hpp'],
    load: () => Promise.resolve(cpp())
  }),
  
  LanguageDescription.of({
    name: 'CSS',
    alias: ['css'],
    extensions: ['css'],
    load: () => Promise.resolve(css())
  }),
  
  LanguageDescription.of({
    name: 'HTML',
    alias: ['html', 'htm'],
    extensions: ['html', 'htm'],
    load: () => Promise.resolve(html())
  }),
  
  LanguageDescription.of({
    name: 'JSON',
    alias: ['json'],
    extensions: ['json'],
    load: () => Promise.resolve(json())
  }),
  
  LanguageDescription.of({
    name: 'Markdown',
    alias: ['md', 'markdown'],
    extensions: ['md', 'markdown'],
    load: () => Promise.resolve(markdown())
  }),
  
  LanguageDescription.of({
    name: 'SQL',
    alias: ['sql'],
    extensions: ['sql'],
    load: () => Promise.resolve(sql())
  }),
  
  LanguageDescription.of({
    name: 'XML',
    alias: ['xml'],
    extensions: ['xml'],
    load: () => Promise.resolve(xml())
  })
]

/**
 * 获取所有支持的语言名称
 */
export const getSupportedLanguages = (): string[] => {
  return codeMirrorLanguages.flatMap(lang => [
    lang.name.toLowerCase(),
    ...(lang.alias || [])
  ])
}

/**
 * 根据语言名称查找对应的语言描述
 */
export const findLanguageByName = (name: string): LanguageDescription | undefined => {
  const lowerName = name.toLowerCase()
  return codeMirrorLanguages.find(lang => 
    lang.name.toLowerCase() === lowerName || 
    (lang.alias && lang.alias.includes(lowerName))
  )
}