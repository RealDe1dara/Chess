import { useEffect, useState } from 'react'
import AuthFormCard from '../components/AuthFormCard'
import type { AuthResponse, User } from '../types/auth'
import '../css/login-page.css'

function LoginPage() {
  const [user, setUser] = useState<User | null>(null)
  const [status, setStatus] = useState<string>('Checking session...')
  const [statusKind, setStatusKind] = useState<'ok' | 'error'>('ok')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)

  useEffect(() => {
    void refreshSession()
  }, [])

  const setMessage = (message: string, kind: 'ok' | 'error' = 'ok') => {
    setStatus(message)
    setStatusKind(kind)
  }

  const readResponse = async (response: Response): Promise<AuthResponse | null> => {
    try {
      return (await response.json()) as AuthResponse
    } catch {
      return null
    }
  }

  const refreshSession = async () => {
    const response = await fetch('/api/auth/me', { credentials: 'include' })
    const body = await readResponse(response)
    if (body?.authenticated && body.user) {
      setUser(body.user)
      setUsername(body.user.username)
      setMessage(`Signed in as ${body.user.username}.`)
      return
    }

    setUser(null)
    setMessage('Not signed in yet.')
  }

  const handleAuth = async (endpoint: '/api/auth/login' | '/api/auth/register') => {
    const response = await fetch(endpoint, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    })

    const body = await readResponse(response)
    if (!response.ok || !body?.authenticated || !body.user) {
      setMessage(body?.message ?? 'Authentication failed.', 'error')
      return
    }

    setUser(body.user)
    setUsername(body.user.username)
    setMessage(body.message ?? 'Success.')
  }

  const handleLogout = async () => {
    await fetch('/api/auth/logout', {
      method: 'POST',
      credentials: 'include',
    })

    setUser(null)
    setPassword('')
    setMessage('Logged out.')
  }

  return (
    <main className="login-page">
      <AuthFormCard
        user={user}
        username={username}
        password={password}
        showPassword={showPassword}
        status={status}
        statusKind={statusKind}
        onUsernameChange={setUsername}
        onPasswordChange={setPassword}
        onTogglePassword={() => setShowPassword((value) => !value)}
        onLogin={() => void handleAuth('/api/auth/login')}
        onRegister={() => void handleAuth('/api/auth/register')}
        onLogout={() => void handleLogout()}
      />
    </main>
  )
}

export default LoginPage
