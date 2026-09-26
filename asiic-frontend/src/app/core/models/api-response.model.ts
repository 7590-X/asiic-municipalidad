export interface ApiResponse<T = any> {
  code: number;
  action: string;
  dateTime?: string;
  datetime?: string;
  message: string;
  payload: T;
}

export type ApiResponseModel = ApiResponse<any>;