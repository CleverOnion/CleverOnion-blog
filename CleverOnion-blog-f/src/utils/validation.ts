import { ValidationResult } from "../hooks/useFormValidation";

/**
 * 文章表单验证规则
 */
export const articleValidationRules = {
  /**
   * 验证文章标题
   */
  title: (value: string): ValidationResult => {
    const trimmedValue = value.trim();

    if (trimmedValue.length === 0) {
      return {
        isValid: false,
        message: "请输入文章标题",
      };
    }

    if (trimmedValue.length > 200) {
      return {
        isValid: false,
        message: "标题不能超过 200 个字符",
      };
    }

    if (trimmedValue.length < 2) {
      return {
        isValid: false,
        message: "标题至少需要 2 个字符",
      };
    }

    return {
      isValid: true,
      message: "",
    };
  },

  /**
   * 验证文章内容
   */
  content: (value: string): ValidationResult => {
    const trimmedValue = value.trim();

    if (trimmedValue.length === 0) {
      return {
        isValid: false,
        message: "请输入文章内容",
      };
    }

    if (trimmedValue.length < 10) {
      return {
        isValid: false,
        message: "文章内容至少需要 10 个字符",
      };
    }

    return {
      isValid: true,
      message: "",
    };
  },

  /**
   * 验证文章分类
   */
  category_id: (value: number | null): ValidationResult => {
    if (value === null || value === undefined) {
      return {
        isValid: false,
        message: "请选择文章分类",
      };
    }

    if (value <= 0) {
      return {
        isValid: false,
        message: "请选择有效的文章分类",
      };
    }

    return {
      isValid: true,
      message: "",
    };
  },

  /**
   * 验证文章摘要（可选字段）
   */
  summary: (value: string | undefined): ValidationResult => {
    if (!value) {
      // 摘要是可选的
      return {
        isValid: true,
        message: "",
      };
    }

    const trimmedValue = value.trim();

    if (trimmedValue.length > 500) {
      return {
        isValid: false,
        message: "摘要不能超过 500 个字符",
      };
    }

    return {
      isValid: true,
      message: "",
    };
  },
};

/**
 * 通用验证规则
 */
export const commonValidationRules = {
  /**
   * 必填验证
   */
  required:
    (fieldName: string) =>
    (value: unknown): ValidationResult => {
      const isEmpty =
        value === null ||
        value === undefined ||
        (typeof value === "string" && value.trim().length === 0) ||
        (Array.isArray(value) && value.length === 0);

      if (isEmpty) {
        return {
          isValid: false,
          message: `${fieldName}不能为空`,
        };
      }

      return {
        isValid: true,
        message: "",
      };
    },

  /**
   * 最小长度验证
   */
  minLength:
    (min: number, fieldName: string) =>
    (value: string): ValidationResult => {
      if (value && value.trim().length < min) {
        return {
          isValid: false,
          message: `${fieldName}至少需要 ${min} 个字符`,
        };
      }

      return {
        isValid: true,
        message: "",
      };
    },

  /**
   * 最大长度验证
   */
  maxLength:
    (max: number, fieldName: string) =>
    (value: string): ValidationResult => {
      if (value && value.trim().length > max) {
        return {
          isValid: false,
          message: `${fieldName}不能超过 ${max} 个字符`,
        };
      }

      return {
        isValid: true,
        message: "",
      };
    },

  /**
   * 组合验证器
   */
  combine:
    (...validators: Array<(value: unknown) => ValidationResult>) =>
    (value: unknown): ValidationResult => {
      for (const validator of validators) {
        const result = validator(value);
        if (!result.isValid) {
          return result;
        }
      }
      return {
        isValid: true,
        message: "",
      };
    },
};
