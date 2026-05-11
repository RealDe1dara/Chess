import { useCallback, useEffect, useState } from 'react'
import AuthFormCard from '../components/AuthFormCard'
import ToastStack from '../components/menu/ToastStack'
import useToasts from '../hooks/useToasts'
import type { AuthResponse, User } from '../types/auth'
import type { GameState } from '../types/game'
import { setToken, clearToken, authHeaders } from '../utils/auth'
import MainMenuPage from './MainMenuPage'
import ChessGamePage from './ChessGamePage'
import '../css/login-page.css'

const ACTIVE_GAME_KEY = 'chess_active_game_id'

function LoginPage() {
  const [user, setUser] = useState<User | null>(null)
  const [activeGameId, setActiveGameId] = useState<number | null>(null)
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
    const response = await fetch('/api/auth/me', { headers: authHeaders() })
    const body = await readResponse(response)
    if (body?.authenticated && body.user) {
      setUser(body.user)
      setUsername(body.user.username)
      pushToast(`Signed in as ${body.user.username}.`)

      const savedId = sessionStorage.getItem(ACTIVE_GAME_KEY)
      if (savedId) {
        const id = Number(savedId)
        try {
          const gr = await fetch(`/api/game/${id}/state`, { headers: authHeaders() })
          if (gr.ok) {
            const gs = (await gr.json()) as GameState
            const un = body.user.username
            const isPlayer = gs.playerWhite === un || gs.playerBlack === un
            if (isPlayer && (gs.status === 'ACTIVE' || gs.status === 'WAITING')) {
              setActiveGameId(id)
            } else {
              sessionStorage.removeItem(ACTIVE_GAME_KEY)
            }
          } else {
            sessionStorage.removeItem(ACTIVE_GAME_KEY)
          }
        } catch {
          sessionStorage.removeItem(ACTIVE_GAME_KEY)
        }
      }
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
      // No credentials: 'include' — we don't use cookies anymore.
      // The token comes back in the response body, not as a Set-Cookie header.
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    })

    const body = await readResponse(response)
    if (!response.ok || !body?.authenticated || !body.user) {
      pushToast(body?.message ?? 'Authentication failed.', 'error')
      return
    }

    if (body.token) {
      setToken(body.token)
    }

    setUser(body.user)
    setUsername(body.user.username)
    pushToast(body.message ?? 'Success.')
  }

  const handleStartGame = useCallback((gameId: number) => {
    sessionStorage.setItem(ACTIVE_GAME_KEY, String(gameId))
    setActiveGameId(gameId)
  }, [])

  const handleLeaveGame = useCallback(() => {
    sessionStorage.removeItem(ACTIVE_GAME_KEY)
    setActiveGameId(null)
  }, [])

  const handleLogout = async () => {
    await fetch('/api/auth/logout', {
      method: 'POST',
      headers: authHeaders(),
    })

    clearToken()
    sessionStorage.removeItem(ACTIVE_GAME_KEY)
    setUser(null)
    setActiveGameId(null)
    setPassword('')
    pushToast('Logged out.')
  }

  return (
    <main className={`login-page${!user ? ' centered' : ''}`}>
      {user && activeGameId !== null ? (
        <ChessGamePage
          gameId={activeGameId}
          username={user.username}
          onLeave={handleLeaveGame}
        />
      ) : user ? (
        <MainMenuPage
          user={user}
          onUserChange={setUser}
          onLogout={() => void handleLogout()}
          onStartGame={handleStartGame}
        />
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
          onValidationError={(msg) => pushToast(msg, 'error')}
        />
      )}
      <ToastStack toasts={toasts} />
    </main>
  )
}

export default LoginPage
