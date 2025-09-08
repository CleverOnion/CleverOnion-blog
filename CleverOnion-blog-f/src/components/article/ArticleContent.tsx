import React, { forwardRef } from 'react';
import { motion } from 'framer-motion';
import ReactMarkdown from 'react-markdown';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { tomorrow } from 'react-syntax-highlighter/dist/esm/styles/prism';

interface ArticleContentProps {
  content?: string;
}

const ArticleContent = forwardRef<HTMLElement, ArticleContentProps>(({ content }, ref) => {
  return (
    <motion.article 
      ref={ref}
      className="flex-1 bg-white/80 backdrop-blur-sm rounded-lg p-8"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6, ease: "easeOut" }}
    >
      <div className="prose prose-slate prose-xl dark:prose-invert">
        {content ? (
          <ReactMarkdown
            components={{
              code({ node, inline, className, children, ...props }: any) {
                const match = /language-(\w+)/.exec(className || '');
                return !inline && match ? (
                  <SyntaxHighlighter
                    style={tomorrow}
                    language={match[1]}
                    PreTag="div"
                    className="rounded-lg my-6 shadow-sm scrollbar-thin scrollbar-thumb-gray-600 scrollbar-track-gray-800 hover:scrollbar-thumb-gray-500"
                    customStyle={{
                      background: 'transparent',
                      backgroundColor: 'transparent'
                    }}
                    {...props}
                  >
                    {String(children).replace(/\n$/, '')}
                  </SyntaxHighlighter>
                ) : (
                  <code className={className} {...props}>
                    {children}
                  </code>
                );
              },
              // 为标题元素添加ID，用于目录导航
              h1({ children, ...props }) {
                const text = String(children);
                const id = text
                  .toLowerCase()
                  .replace(/[^\w\s-]/g, '')
                  .replace(/\s+/g, '-')
                  .replace(/-+/g, '-')
                  .replace(/^-|-$/g, '');
                return <h1 id={id} {...props}>{children}</h1>;
              },
              h2({ children, ...props }) {
                const text = String(children);
                const id = text
                  .toLowerCase()
                  .replace(/[^\w\s-]/g, '')
                  .replace(/\s+/g, '-')
                  .replace(/-+/g, '-')
                  .replace(/^-|-$/g, '');
                return <h2 id={id} {...props}>{children}</h2>;
              },
              h3({ children, ...props }) {
                const text = String(children);
                const id = text
                  .toLowerCase()
                  .replace(/[^\w\s-]/g, '')
                  .replace(/\s+/g, '-')
                  .replace(/-+/g, '-')
                  .replace(/^-|-$/g, '');
                return <h3 id={id} {...props}>{children}</h3>;
              },
              h4({ children, ...props }) {
                const text = String(children);
                const id = text
                  .toLowerCase()
                  .replace(/[^\w\s-]/g, '')
                  .replace(/\s+/g, '-')
                  .replace(/-+/g, '-')
                  .replace(/^-|-$/g, '');
                return <h4 id={id} {...props}>{children}</h4>;
              },
              h5({ children, ...props }) {
                const text = String(children);
                const id = text
                  .toLowerCase()
                  .replace(/[^\w\s-]/g, '')
                  .replace(/\s+/g, '-')
                  .replace(/-+/g, '-')
                  .replace(/^-|-$/g, '');
                return <h5 id={id} {...props}>{children}</h5>;
              },
              h6({ children, ...props }) {
                const text = String(children);
                const id = text
                  .toLowerCase()
                  .replace(/[^\w\s-]/g, '')
                  .replace(/\s+/g, '-')
                  .replace(/-+/g, '-')
                  .replace(/^-|-$/g, '');
                return <h6 id={id} {...props}>{children}</h6>;
              },
            }}
          >
            {content}
          </ReactMarkdown>
        ) : (
          <div className="text-center text-gray-500 py-8">
            <p>暂无内容</p>
          </div>
        )}
      </div>
    </motion.article>
  );
});

ArticleContent.displayName = 'ArticleContent';

export default ArticleContent;