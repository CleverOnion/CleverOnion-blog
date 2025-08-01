// Import refractor with all languages
import { refractor } from 'refractor/all'

/**
 * Configure Prism languages for the Milkdown editor
 * Using refractor/lib/all.js automatically includes all 290+ supported languages
 * No manual registration needed as all languages are pre-registered
 */
export function configurePrismLanguages() {
  // All languages are automatically available when using refractor/lib/all.js
  // This includes 290+ programming languages supported by Prism.js
  console.log('All Prism languages loaded automatically via refractor/lib/all.js')
}

/**
 * List of commonly used programming languages
 * Note: When using refractor/lib/all.js, all 290+ languages are automatically supported
 * This list is for reference only and represents the most commonly used languages
 */
export const supportedLanguages = [
  'plain', 'text', 'markdown', 'css', 'html', 'xml', 'json', 'yaml',
  'javascript', 'typescript', 'jsx', 'tsx',
  'c', 'cpp', 'java', 'csharp', 'go', 'rust', 'swift', 'kotlin',
  'python', 'php', 'ruby', 'perl', 'lua',
  'sql', 'bash', 'powershell', 'shell',
  'dockerfile', 'nginx', 'apache',
  'r', 'matlab', 'scala', 'haskell', 'clojure',
  'dart', 'elixir', 'erlang', 'fsharp',
  'graphql', 'protobuf', 'toml', 'ini'
] as const

/**
 * Type definition for supported languages
 */
export type SupportedLanguage = typeof supportedLanguages[number]