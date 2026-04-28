import { useCallback, useEffect, useState } from 'react'
import AuthFormCard from '../components/AuthFormCard'
import ToastStack from '../components/menu/ToastStack'
import useToasts from '../hooks/useToasts'
import type { AuthResponse, User } from '../types/auth'
import MainMenuPage from './MainMenuPage'
import '../css/login-page.css'

function LoginPage() {
  const [user, setUser] = useState<User | null>(null)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const { toasts, pushToast } = useToasts()

  const readResponse = useCallback(async (response: Response): Promise<AuthResponse | null> => {
    try {
      return (await response.json()) as AuthResponse
    } catch {
      return null
    }
  }, [])

  const refreshSession = useCallback(async () => {
    const response = await fetch('/api/auth/me', { credentials: 'include' })
    const body = await readResponse(response)
    if (body?.authenticated && body.user) {
      setUser(body.user)
      setUsername(body.user.username)
      pushToast(`Signed in as ${body.user.username}.`)
      return
    }

    setUser(null)
  }, [pushToast, readResponse])

  useEffect(() => {
    const timerId = window.setTimeout(() => {
      void refreshSession()
    }, 0)
    return () => window.clearTimeout(timerId)
  }, [refreshSession])

  const handleAuth = async (endpoint: '/api/auth/login' | '/api/auth/register') => {
    const response = await fetch(endpoint, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    })

    const body = await readResponse(response)
    if (!response.ok || !body?.authenticated || !body.user) {
      pushToast(body?.message ?? 'Authentication failed.', 'error')
      return
    }

    setUser(body.user)
    setUsername(body.user.username)
    pushToast(body.message ?? 'Success.')
  }

  const handleLogout = async () => {
    await fetch('/api/auth/logout', {
      method: 'POST',
      credentials: 'include',
    })

    setUser(null)
    setPassword('')
    pushToast('Logged out.')
  }

  return (
    <main className="login-page">
      {user ? (
        <MainMenuPage user={user} onUserChange={setUser} onLogout={() => void handleLogout()} />
      ) : (
        <AuthFormCard
          user={user}
          username={username}
          password={password}
          showPassword={showPassword}
          onUsernameChange={setUsername}
          onPasswordChange={setPassword}
          onTogglePassword={() => setShowPassword((value) => !value)}
          onLogin={() => void handleAuth('/api/auth/login')}
          onRegister={() => void handleAuth('/api/auth/register')}
          onLogout={() => void handleLogout()}
        />
      )}
      <ToastStack toasts={toasts} />
    </main>
  )
}

export default LoginPage
