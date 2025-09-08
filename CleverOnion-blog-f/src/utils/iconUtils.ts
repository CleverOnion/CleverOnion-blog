import React from 'react';
import { 
  FaCss3Alt, 
  FaReact, 
  FaJs, 
  FaBriefcase,
  FaCode,
  FaDatabase,
  FaMobile,
  FaDesktop,
  FaServer,
  FaCloud,
  FaGitAlt,
  FaNodeJs,
  FaPython,
  FaJava,
  FaPhp,
  FaHtml5,
  FaVuejs,
  FaAngular,
  FaBootstrap,
  FaSass,
  FaLess,
  FaWordpress,
  FaShopify,
  FaDocker,
  FaLinux,
  FaWindows,
  FaApple,
  FaAndroid,
  FaGithub,
  FaGitlab,
  FaBitbucket
} from 'react-icons/fa';

import { 
  MdAnimation, 
  MdList,
  MdWeb,
  MdPhoneAndroid,
  MdDesktopMac,
  MdStorage,
  MdSecurity,
  MdBusiness,
  MdSchool,
  MdTrendingUp,
  MdSettings,
  MdBuild,
  MdCode,
  MdDesignServices
} from 'react-icons/md';

import {
  SiTypescript,
  SiNextdotjs,
  SiTailwindcss,
  SiMongodb,
  SiMysql,
  SiPostgresql,
  SiRedis,
  SiGraphql,
  SiWebpack,
  SiVite,
  SiEslint,
  SiPrettier,
  SiJest,
  SiCypress,
  SiStorybook,
  SiFigma,
  SiSketch,
  SiAdobexd
} from 'react-icons/si';

// 图标映射表
export const iconMap: { [key: string]: React.ComponentType<any> } = {
  // Font Awesome Icons
  FaCss3Alt,
  FaReact,
  FaJs,
  FaBriefcase,
  FaCode,
  FaDatabase,
  FaMobile,
  FaDesktop,
  FaServer,
  FaCloud,
  FaGitAlt,
  FaNodeJs,
  FaPython,
  FaJava,
  FaPhp,
  FaHtml5,
  FaVuejs,
  FaAngular,
  FaBootstrap,
  FaSass,
  FaLess,
  FaWordpress,
  FaShopify,
  FaDocker,
  FaLinux,
  FaWindows,
  FaApple,
  FaAndroid,
  FaGithub,
  FaGitlab,
  FaBitbucket,
  
  // Material Design Icons
  MdAnimation,
  MdList,
  MdWeb,
  MdPhoneAndroid,
  MdDesktopMac,
  MdStorage,
  MdSecurity,
  MdBusiness,
  MdSchool,
  MdTrendingUp,
  MdSettings,
  MdBuild,
  MdCode,
  MdDesignServices,
  
  // Simple Icons
  SiTypescript,
  SiNextdotjs,
  SiTailwindcss,
  SiMongodb,
  SiMysql,
  SiPostgresql,
  SiRedis,
  SiGraphql,
  SiWebpack,
  SiVite,
  SiEslint,
  SiPrettier,
  SiJest,
  SiCypress,
  SiStorybook,
  SiFigma,
  SiSketch,
  SiAdobexd
};

/**
 * 根据图标名称获取对应的React图标组件
 * @param iconName 图标名称，如 'FaReact', 'MdAnimation'
 * @returns React图标组件或默认图标
 */
export const getIconComponent = (iconName?: string): React.ComponentType<any> => {
  if (!iconName || !iconMap[iconName]) {
    return MdList; // 默认图标
  }
  return iconMap[iconName];
};

/**
 * 渲染图标组件
 * @param iconName 图标名称
 * @param className CSS类名
 * @param size 图标大小
 * @returns JSX元素
 */
export const renderIcon = (
  iconName?: string, 
  className?: string, 
  size?: number | string
): React.ReactElement => {
  const IconComponent = getIconComponent(iconName);
  return React.createElement(IconComponent, { 
    className, 
    size,
    'aria-hidden': true 
  });
};

/**
 * 获取所有可用的图标列表
 * @returns 图标名称数组
 */
export const getAvailableIcons = (): string[] => {
  return Object.keys(iconMap);
};

/**
 * 按分类获取图标
 */
export const getIconsByCategory = () => {
  return {
    frontend: [
      'FaReact', 'FaVuejs', 'FaAngular', 'FaHtml5', 'FaCss3Alt', 'FaJs', 
      'SiTypescript', 'SiNextdotjs', 'SiTailwindcss', 'FaBootstrap', 'FaSass', 'FaLess'
    ],
    backend: [
      'FaNodeJs', 'FaPython', 'FaJava', 'FaPhp', 'FaServer', 'FaDatabase',
      'SiMongodb', 'SiMysql', 'SiPostgresql', 'SiRedis', 'SiGraphql'
    ],
    mobile: [
      'FaMobile', 'FaAndroid', 'FaApple', 'MdPhoneAndroid'
    ],
    tools: [
      'FaGitAlt', 'FaGithub', 'FaGitlab', 'FaBitbucket', 'FaDocker',
      'SiWebpack', 'SiVite', 'SiEslint', 'SiPrettier', 'SiJest', 'SiCypress'
    ],
    design: [
      'SiFigma', 'SiSketch', 'SiAdobexd', 'MdDesignServices'
    ],
    business: [
      'FaBriefcase', 'MdBusiness', 'MdTrendingUp', 'MdSchool'
    ],
    general: [
      'MdList', 'MdCode', 'MdWeb', 'MdAnimation', 'MdSettings', 'MdBuild'
    ]
  };
};