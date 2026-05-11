export type User = {
  id: number
  username: string
}

export type AuthResponse = {
  authenticated: boolean
  user: User | null
  message: string | null
  token: string | null
}
