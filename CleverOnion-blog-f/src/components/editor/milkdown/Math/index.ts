/**
 * Math Plugin Configuration for Milkdown
 *
 * Provides LaTeX math support in the editor using KaTeX
 * Supports both inline math ($...$) and block math ($$...$$)
 */

import { math } from "@milkdown/plugin-math";
import type { MilkdownPlugin } from "@milkdown/kit/ctx";

/**
 * Math plugin with default configuration
 *
 * Usage in markdown:
 * - Inline math: $E = mc^2$
 * - Block math:
 *   $$
 *   \int_{-\infty}^{\infty} e^{-x^2} dx = \sqrt{\pi}
 *   $$
 */
export const mathPlugin: MilkdownPlugin[] = math;

// Re-export for convenience
export { math };
