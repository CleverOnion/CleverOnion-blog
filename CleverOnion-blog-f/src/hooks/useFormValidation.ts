import { useState, useCallback, useRef } from "react";

/**
 * 验证结果接口
 */
export interface ValidationResult {
  isValid: boolean;
  message: string;
}

/**
 * 字段验证状态
 */
export interface FieldValidation {
  isValid: boolean;
  message: string;
  isDirty: boolean; // 是否已编辑过
  isTouched: boolean; // 是否已失去焦点
}

/**
 * 验证规则
 */
export type ValidationRule<T = unknown> = (value: T) => ValidationResult;

/**
 * 表单验证配置
 */
export interface ValidationConfig {
  [key: string]: ValidationRule;
}

/**
 * 表单验证 Hook
 * 提供实时验证、触发验证和聚焦到错误字段的功能
 */
export function useFormValidation<T extends Record<string, unknown>>(
  _initialValues: T,
  validationConfig: ValidationConfig
) {
  // 验证状态
  const [validationState, setValidationState] = useState<
    Record<string, FieldValidation>
  >(() => {
    const initialState: Record<string, FieldValidation> = {};
    Object.keys(validationConfig).forEach((key) => {
      initialState[key] = {
        isValid: true,
        message: "",
        isDirty: false,
        isTouched: false,
      };
    });
    return initialState;
  });

  // 字段引用，用于聚焦
  const fieldRefs = useRef<Record<string, HTMLElement | null>>({});

  /**
   * 验证单个字段
   */
  const validateField = useCallback(
    (fieldName: string, value: unknown): ValidationResult => {
      const validator = validationConfig[fieldName];
      if (!validator) {
        return { isValid: true, message: "" };
      }
      return validator(value);
    },
    [validationConfig]
  );

  /**
   * 更新字段验证状态
   */
  const updateFieldValidation = useCallback(
    (
      fieldName: string,
      value: unknown,
      options: { dirty?: boolean; touched?: boolean } = {}
    ) => {
      const result = validateField(fieldName, value);
      setValidationState((prev) => ({
        ...prev,
        [fieldName]: {
          isValid: result.isValid,
          message: result.message,
          isDirty:
            options.dirty !== undefined
              ? options.dirty
              : prev[fieldName]?.isDirty || false,
          isTouched:
            options.touched !== undefined
              ? options.touched
              : prev[fieldName]?.isTouched || false,
        },
      }));
      return result;
    },
    [validateField]
  );

  /**
   * 标记字段为已编辑
   */
  const markFieldAsDirty = useCallback((fieldName: string) => {
    setValidationState((prev) => ({
      ...prev,
      [fieldName]: {
        ...prev[fieldName],
        isDirty: true,
      },
    }));
  }, []);

  /**
   * 标记字段为已失去焦点
   */
  const markFieldAsTouched = useCallback((fieldName: string) => {
    setValidationState((prev) => ({
      ...prev,
      [fieldName]: {
        ...prev[fieldName],
        isTouched: true,
      },
    }));
  }, []);

  /**
   * 验证所有字段
   */
  const validateAllFields = useCallback(
    (values: T): boolean => {
      let isFormValid = true;
      const newValidationState: Record<string, FieldValidation> = {};

      Object.keys(validationConfig).forEach((fieldName) => {
        const result = validateField(fieldName, values[fieldName]);
        newValidationState[fieldName] = {
          isValid: result.isValid,
          message: result.message,
          isDirty: true,
          isTouched: true,
        };

        if (!result.isValid) {
          isFormValid = false;
        }
      });

      setValidationState(newValidationState);
      return isFormValid;
    },
    [validationConfig, validateField]
  );

  /**
   * 聚焦到第一个错误字段
   */
  const focusFirstError = useCallback(() => {
    const firstErrorField = Object.keys(validationState).find(
      (key) => !validationState[key].isValid && validationState[key].isTouched
    );

    if (firstErrorField && fieldRefs.current[firstErrorField]) {
      const element = fieldRefs.current[firstErrorField];
      element?.focus();
      // 滚动到视图
      element?.scrollIntoView({ behavior: "smooth", block: "center" });
    }
  }, [validationState]);

  /**
   * 注册字段引用
   */
  const registerField = useCallback(
    (fieldName: string, ref: HTMLElement | null) => {
      fieldRefs.current[fieldName] = ref;
    },
    []
  );

  /**
   * 重置验证状态
   */
  const resetValidation = useCallback(() => {
    const resetState: Record<string, FieldValidation> = {};
    Object.keys(validationConfig).forEach((key) => {
      resetState[key] = {
        isValid: true,
        message: "",
        isDirty: false,
        isTouched: false,
      };
    });
    setValidationState(resetState);
  }, [validationConfig]);

  /**
   * 检查表单是否有错误
   */
  const hasErrors = useCallback((): boolean => {
    return Object.values(validationState).some(
      (field) => !field.isValid && field.isTouched
    );
  }, [validationState]);

  /**
   * 获取所有错误信息
   */
  const getErrors = useCallback((): Record<string, string> => {
    const errors: Record<string, string> = {};
    Object.entries(validationState).forEach(([key, field]) => {
      if (!field.isValid && field.isTouched) {
        errors[key] = field.message;
      }
    });
    return errors;
  }, [validationState]);

  return {
    validationState,
    validateField,
    updateFieldValidation,
    markFieldAsDirty,
    markFieldAsTouched,
    validateAllFields,
    focusFirstError,
    registerField,
    resetValidation,
    hasErrors,
    getErrors,
  };
}
