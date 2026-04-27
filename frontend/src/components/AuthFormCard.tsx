import SessionInfo from './SessionInfo'
import type { User } from '../types/auth'
import '../css/auth-form-card.css'

type AuthFormCardProps = {
  user: User | null
  username: string
  password: string
  showPassword: boolean
  status: string
  statusKind: 'ok' | 'error'
  onUsernameChange: (value: string) => void
  onPasswordChange: (value: string) => void
  onTogglePassword: () => void
  onLogin: () => void
  onRegister: () => void
  onLogout: () => void
}

function AuthFormCard({
  user,
  username,
  password,
  showPassword,
  status,
  statusKind,
  onUsernameChange,
  onPasswordChange,
  onTogglePassword,
  onLogin,
  onRegister,
  onLogout,
}: AuthFormCardProps) {
  return (
    <section className="auth-form-card">
      <h1>Welcome to CalmChess</h1>
      <p className="subtitle">Sign in to continue.</p>

      <label className="field">
        <span>Username</span>
        <input
          placeholder="Enter your username"
          value={username}
          onChange={(event) => onUsernameChange(event.target.value)}
          minLength={3}
          required
        />
      </label>

      <label className="field password-field">
        <span>Password</span>
        <input
          type={showPassword ? 'text' : 'password'}
          placeholder="Enter your password"
          value={password}
          onChange={(event) => onPasswordChange(event.target.value)}
          minLength={6}
          required
        />
        <button type="button" className="eye-btn" aria-label="Toggle password visibility" onClick={onTogglePassword}>
          👁
        </button>
      </label>

      <div className="actions">
        <button type="button" className="primary-btn" onClick={onLogin}>
          Log in
        </button>
        <button type="button" className="secondary-btn" onClick={onRegister}>
          Create account
        </button>
      </div>

      <p className={`status-line ${statusKind}`}>{status}</p>
      {user && <SessionInfo username={user.username} onLogout={onLogout} />}
    </section>
  )
}

export default AuthFormCard
