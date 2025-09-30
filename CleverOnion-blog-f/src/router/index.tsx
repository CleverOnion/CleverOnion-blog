import { createBrowserRouter } from "react-router-dom";
import { lazy } from "react";
import Layout from "../components/Layout";
import AdminLayout from "../components/AdminLayout";
import SuspenseWrapper from "../components/common/SuspenseWrapper";

// 首页不做懒加载，保证首屏加载速度
import Home from "../pages/Home";

// 使用 React.lazy 进行代码分割
const Category = lazy(() => import("../pages/Category"));
const Article = lazy(() => import("../pages/Article"));
const AdminTest = lazy(() => import("../pages/AdminTest"));
const AuthCallback = lazy(() => import("../pages/AuthCallback"));
const NotFound = lazy(() => import("../pages/NotFound"));

// 管理后台页面 - 延迟加载
const AdminDashboard = lazy(() => import("../pages/admin/Dashboard"));
const UserManagement = lazy(() => import("../pages/admin/UserManagement"));
const ArticleManagement = lazy(
  () => import("../pages/admin/ArticleManagement")
);
const CategoryManagement = lazy(
  () => import("../pages/admin/CategoryManagement")
);
const TagManagement = lazy(() => import("../pages/admin/TagManagement"));
const ArticleEditor = lazy(() => import("../pages/admin/ArticleEditor"));

export const router = createBrowserRouter([
  {
    path: "/",
    element: <Layout />,
    children: [
      {
        index: true,
        element: <Home />,
      },
      {
        path: "category/:categoryId",
        element: (
          <SuspenseWrapper>
            <Category />
          </SuspenseWrapper>
        ),
      },
      {
        path: "article/:articleId",
        element: (
          <SuspenseWrapper>
            <Article />
          </SuspenseWrapper>
        ),
      },
      {
        path: "admin-test",
        element: (
          <SuspenseWrapper>
            <AdminTest />
          </SuspenseWrapper>
        ),
      },
      {
        path: "auth/callback",
        element: (
          <SuspenseWrapper>
            <AuthCallback />
          </SuspenseWrapper>
        ),
      },
      {
        path: "*",
        element: (
          <SuspenseWrapper>
            <NotFound />
          </SuspenseWrapper>
        ),
      },
    ],
  },
  {
    path: "/admin",
    element: <AdminLayout />,
    children: [
      {
        index: true,
        element: (
          <SuspenseWrapper>
            <AdminDashboard />
          </SuspenseWrapper>
        ),
      },
      {
        path: "users",
        element: (
          <SuspenseWrapper>
            <UserManagement />
          </SuspenseWrapper>
        ),
      },
      {
        path: "articles",
        element: (
          <SuspenseWrapper>
            <ArticleManagement />
          </SuspenseWrapper>
        ),
      },
      {
        path: "categories",
        element: (
          <SuspenseWrapper>
            <CategoryManagement />
          </SuspenseWrapper>
        ),
      },
      {
        path: "tags",
        element: (
          <SuspenseWrapper>
            <TagManagement />
          </SuspenseWrapper>
        ),
      },
    ],
  },
  {
    path: "/admin/articles/new",
    element: (
      <SuspenseWrapper>
        <ArticleEditor />
      </SuspenseWrapper>
    ),
  },
  {
    path: "/admin/articles/edit/:articleId",
    element: (
      <SuspenseWrapper>
        <ArticleEditor />
      </SuspenseWrapper>
    ),
  },
  {
    path: "*",
    element: (
      <SuspenseWrapper>
        <NotFound />
      </SuspenseWrapper>
    ),
  },
]);
