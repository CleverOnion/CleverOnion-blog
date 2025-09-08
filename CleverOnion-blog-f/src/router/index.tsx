import { createBrowserRouter } from 'react-router-dom';
import Layout from '../components/Layout';
import AdminLayout from '../components/AdminLayout';
import Home from '../pages/Home';
import Category from '../pages/Category';
import Article from '../pages/Article';
import AdminTest from '../pages/AdminTest';
import AuthCallback from '../pages/AuthCallback';
import NotFound from '../pages/NotFound';
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

      {
        path: 'admin-test',
        element: <AdminTest />
      },
      {
        path: 'auth/callback',
        element: <AuthCallback />
      },
      {
        path: '*',
        element: <NotFound />
      }
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
      }
    ]
  },
  {
    path: '/admin/articles/new',
    element: <ArticleEditor />
  },
  {
    path: '/admin/articles/edit/:articleId',
    element: <ArticleEditor />
  },
  {
    path: '*',
    element: <NotFound />
  }
]);