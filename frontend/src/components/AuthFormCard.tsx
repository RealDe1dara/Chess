import { useState } from 'react'
import type { User } from '../types/auth'
import '../css/auth-form-card.css'

type AuthFormCardProps = {
  user: User | null
  username: string
  password: string
  showPassword: boolean
  onUsernameChange: (value: string) => void
  onPasswordChange: (value: string) => void
  onTogglePassword: () => void
  onLogin: () => void
  onRegister: () => void
  onLogout: () => void
  onValidationError: (msg: string) => void
}

function AuthFormCard({
  username,
  password,
  showPassword,
  onUsernameChange,
  onPasswordChange,
  onTogglePassword,
  onLogin,
  onRegister,
  onValidationError,
}: AuthFormCardProps) {
  const [view, setView] = useState<'login' | 'register'>('login')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  const switchToRegister = () => setView('register')

  const switchToLogin = () => {
    setView('login')
    setConfirmPassword('')
  }

  const handleRegister = () => {
    if (password !== confirmPassword) {
      onValidationError('Passwords do not match.')
      return
    }
    onRegister()
  }

  if (view === 'register') {
    return (
      <section className="auth-form-card">
        <button type="button" className="back-btn" onClick={switchToLogin}>
          ← Back to sign in
        </button>

        <h1>Create account</h1>
        <p className="subtitle">Join CalmChess today.</p>

        <label className="field">
          <span>Username</span>
          <input
            placeholder="Choose a username"
            value={username}
            onChange={(e) => onUsernameChange(e.target.value)}
            minLength={3}
            required
          />
        </label>

        <label className="field password-field">
          <span>Password</span>
          <input
            type={showPassword ? 'text' : 'password'}
            placeholder="Choose a password"
            value={password}
            onChange={(e) => onPasswordChange(e.target.value)}
            minLength={6}
            required
          />
          <button type="button" className="eye-btn" aria-label="Toggle password visibility" onClick={onTogglePassword}>
            👁
          </button>
        </label>

        <label className="field password-field">
          <span>Confirm password</span>
          <input
            type={showConfirmPassword ? 'text' : 'password'}
            placeholder="Repeat your password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            minLength={6}
            required
          />
          <button
            type="button"
            className="eye-btn"
            aria-label="Toggle confirm password visibility"
            onClick={() => setShowConfirmPassword((v) => !v)}
          >
            👁
          </button>
        </label>

        <button type="button" className="primary-btn full-btn" onClick={handleRegister}>
          Create account
        </button>
      </section>
    )
  }

  return (
    <section className="auth-form-card">
      <h1>Welcome to CalmChess</h1>
      <p className="subtitle">Sign in to continue.</p>

      <label className="field">
        <span>Username</span>
        <input
          placeholder="Enter your username"
          value={username}
          onChange={(e) => onUsernameChange(e.target.value)}
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
          onChange={(e) => onPasswordChange(e.target.value)}
          minLength={6}
          required
        />
        <button type="button" className="eye-btn" aria-label="Toggle password visibility" onClick={onTogglePassword}>
          👁
        </button>
      </label>

      <button type="button" className="primary-btn full-btn" onClick={onLogin}>
        Log in
      </button>

      <div className="or-divider">
        <span>or</span>
      </div>

      <button type="button" className="secondary-btn full-btn" onClick={switchToRegister}>
        Create account
      </button>
    </section>
  )
}

export default AuthFormCard
