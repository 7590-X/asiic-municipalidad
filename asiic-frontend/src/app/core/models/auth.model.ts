import { ApiResponse } from './api-response.model';

export interface LoginRequest {
  username: string;
  password?: string;
  recordar?: boolean;
}

export interface AuthTokenPayload {
  access_token: string;
  expires_in: number;
  refresh_expires_in: number;
  token_type: string;
}

export type AuthLoginResponse = ApiResponse<AuthTokenPayload>;

export interface KeycloakTokenClaims {
  sub: string;
  email?: string;
  email_verified?: boolean;
  preferred_username?: string;
  name?: string;
  given_name?: string;
  family_name?: string;
  realm_access?: {
    roles: string[];
  };
  resource_access?: {
    [client: string]: {
      roles: string[];
    };
  };
  exp?: number;
  iat?: number;
  [key: string]: any;
}

export interface CurrentUser {
  id: string;
  email: string;
  username: string;
  name: string;
  roles: string[];
  token: string;
  expiresAt: number;
}
