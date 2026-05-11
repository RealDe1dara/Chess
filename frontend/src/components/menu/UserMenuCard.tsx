import { useState } from 'react'
import type { AuthResponse, User } from '../../types/auth'
import { authHeaders } from '../../utils/auth'
import '../../css/menu/user-menu-card.css'

type UserMenuCardProps = {
  user: User
  elo: number | null
  onUserChange: (user: User) => void
}

function UserMenuCard({ user, elo, onUserChange }: UserMenuCardProps) {
  const [expanded, setExpanded] = useState(false)
  const [newUsername, setNewUsername] = useState(user.username)
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [status, setStatus] = useState('Open your profile card to edit username or password.')
  const [statusKind, setStatusKind] = useState<'ok' | 'error'>('ok')

  const setMessage = (message: string, kind: 'ok' | 'error' = 'ok') => {
    setStatus(message)
    setStatusKind(kind)
  }

  const readAuthResponse = async (response: Response): Promise<AuthResponse | null> => {
    try {
      return (await response.json()) as AuthResponse
    } catch {
      return null
    }
  }

  const handleUsernameSave = async () => {
    const response = await fetch('/api/auth/profile', {
      method: 'PUT',
      headers: { ...authHeaders(), 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: newUsername.trim() }),
    })
    const body = await readAuthResponse(response)
    if (!response.ok || !body?.user) {
      setMessage(body?.message ?? 'Unable to update username.', 'error')
      return
    }
    onUserChange(body.user)
    setNewUsername(body.user.username)
    setMessage(body.message ?? 'Username updated.')
  }

  const handlePasswordChange = async () => {
    const response = await fetch('/api/auth/password', {
      method: 'POST',
      headers: { ...authHeaders(), 'Content-Type': 'application/json' },
      body: JSON.stringify({ currentPassword, newPassword }),
    })
    const body = await readAuthResponse(response)
    if (!response.ok) {
      setMessage(body?.message ?? 'Unable to change password.', 'error')
      return
    }
    setCurrentPassword('')
    setNewPassword('')
    setMessage(body?.message ?? 'Password changed.')
  }

  return (
    <section className="user-menu-card menu-card">
      <button type="button" className="user-chip" onClick={() => setExpanded((value) => !value)}>
        <span className="avatar-circle">{user.username.charAt(0).toUpperCase()}</span>
        <span>
          <strong>{user.username}</strong>
          <small>ELO {elo ?? '—'}</small>
        </span>
      </button>

      {expanded && (
        <div className="profile-panel">
          <h2>Profile settings</h2>
          <label>
            <span>Username</span>
            <input value={newUsername} onChange={(event) => setNewUsername(event.target.value)} minLength={3} />
          </label>
          <button type="button" className="small-btn primary" onClick={() => void handleUsernameSave()}>
            Save name
          </button>

          <label>
            <span>Current password</span>
            <input
              type="password"
              value={currentPassword}
              onChange={(event) => setCurrentPassword(event.target.value)}
              minLength={6}
            />
          </label>
          <label>
            <span>New password</span>
            <input type="password" value={newPassword} onChange={(event) => setNewPassword(event.target.value)} minLength={6} />
          </label>
          <button type="button" className="small-btn" onClick={() => void handlePasswordChange()}>
            Change password
          </button>

          <p className={`profile-status ${statusKind}`}>{status}</p>
        </div>
      )}
    </section>
  )
}

export default UserMenuCard
