import { createBrowserRouter } from 'react-router-dom';
import Layout from '../components/Layout';
import AdminLayout from '../components/AdminLayout';
import Home from '../pages/Home';
import Category from '../pages/Category';
import Article from '../pages/Article';
import AdminDashboard from '../pages/admin/Dashboard';
import UserManagement from '../pages/admin/UserManagement';
import ArticleManagement from '../pages/admin/ArticleManagement';
import CategoryManagement from '../pages/admin/CategoryManagement';
import TagManagement from '../pages/admin/TagManagement';
import ArticleEditor from '../pages/admin/ArticleEditor';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      {
        index: true,
        element: <Home />
      },
      {
        path: 'category/:categoryId',
        element: <Category />
      },
      {
        path: 'article/:articleId',
        element: <Article />
      },
    ]
  },
  {
    path: '/admin',
    element: <AdminLayout />,
    children: [
      {
        index: true,
        element: <AdminDashboard />
      },
      {
        path: 'users',
        element: <UserManagement />
      },
      {
        path: 'articles',
        element: <ArticleManagement />
      },
      {
        path: 'categories',
        element: <CategoryManagement />
      },
      {
        path: 'tags',
        element: <TagManagement />
      },
      {
        path: 'editor',
        element: <ArticleEditor />
      },
      {
        path: 'editor/:articleId',
        element: <ArticleEditor />
      }
    ]
  }
]);