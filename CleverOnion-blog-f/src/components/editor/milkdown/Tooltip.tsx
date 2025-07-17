import { Ctx } from "@milkdown/kit/ctx"
import { tooltipFactory, TooltipProvider } from "@milkdown/kit/plugin/tooltip"
import { 
    toggleStrongCommand, 
    toggleEmphasisCommand, 
    toggleInlineCodeCommand
} from "@milkdown/kit/preset/commonmark"
import { toggleStrikethroughCommand } from "@milkdown/kit/preset/gfm"
import { useInstance } from '@milkdown/react'
import { usePluginViewContext } from "@prosemirror-adapter/react"
import { useCallback, useEffect, useRef } from "react"
import { callCommand } from "@milkdown/kit/utils"
import { FiBold, FiItalic, FiCode } from 'react-icons/fi'
import { RiStrikethrough } from 'react-icons/ri'

export const tooltip = tooltipFactory('Text');

export const TooltipView = () => {
    const ref = useRef<HTMLDivElement>(null)
    const tooltipProvider = useRef<TooltipProvider | undefined>(undefined)

    const { view, prevState } = usePluginViewContext()
    const [loading, get] = useInstance()
    const action = useCallback((fn: (ctx: Ctx) => void) => {
        if (loading) return;
        get().action(fn)
    }, [loading])

    useEffect(() => {
        const div = ref.current
        if (loading || !div) {
            return;
        }
        tooltipProvider.current = new TooltipProvider({
            content: div,
        })

        return () => {
            tooltipProvider.current?.destroy()
        }
    }, [loading])

    useEffect(() => {
        tooltipProvider.current?.update(view, prevState)
    })

    return (
         <div className="absolute data-[show=false]:hidden bg-gray-50 border border-gray-300/60 rounded-lg shadow-lg px-3 py-2 flex items-center gap-1" ref={ref}>
              {/* Bold Button */}
              <button
                  className="text-gray-600 hover:text-gray-800 hover:bg-gray-50 p-2.5 rounded-xl transition-all duration-200 flex items-center justify-center min-w-[40px] h-[40px]"
                  onMouseDown={(e) => {
                      e.preventDefault()
                      action(callCommand(toggleStrongCommand.key))
                  }}
                  title="Bold (Ctrl+B)"
              >
                  <FiBold size={18} strokeWidth={2.5} />
              </button>
              
              {/* Italic Button */}
              <button
                  className="text-gray-600 hover:text-gray-800 hover:bg-gray-50 p-2.5 rounded-xl transition-all duration-200 flex items-center justify-center min-w-[40px] h-[40px]"
                  onMouseDown={(e) => {
                      e.preventDefault()
                      action(callCommand(toggleEmphasisCommand.key))
                  }}
                  title="Italic (Ctrl+I)"
              >
                  <FiItalic size={18} strokeWidth={2.5} />
              </button>
              
              {/* Strikethrough Button */}
              <button
                  className="text-gray-600 hover:text-gray-800 hover:bg-gray-50 p-2.5 rounded-xl transition-all duration-200 flex items-center justify-center min-w-[40px] h-[40px]"
                  onMouseDown={(e) => {
                      e.preventDefault()
                      action(callCommand(toggleStrikethroughCommand.key))
                  }}
                  title="Strikethrough"
              >
                  <RiStrikethrough size={18} strokeWidth={2.5} />
              </button>
              
              {/* Divider */}
              <div className="w-px h-6 bg-gray-200 mx-1"></div>
              
              {/* Code Button */}
              <button
                  className="text-gray-600 hover:text-gray-800 hover:bg-gray-50 p-2.5 rounded-xl transition-all duration-200 flex items-center justify-center min-w-[40px] h-[40px]"
                  onMouseDown={(e) => {
                      e.preventDefault()
                      action(callCommand(toggleInlineCodeCommand.key))
                  }}
                  title="Inline Code (Ctrl+`)"
              >
                  <FiCode size={18} strokeWidth={2.5} />
              </button>
              
          </div>
    )
}
