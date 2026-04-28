import { useEffect, useState } from 'react'
import { FaMedal, FaTimes } from 'react-icons/fa'
import type { AuthResponse, User } from '../../types/auth'
import '../../css/menu/profile-modal.css'

type ProfileModalProps = {
  user: User
  elo: number | null
  open: boolean
  onClose: () => void
  onUserChange: (user: User) => void
  onNotify: (message: string, kind?: 'ok' | 'error') => void
}

function ProfileModal({ user, elo, open, onClose, onUserChange, onNotify }: ProfileModalProps) {
  const [newUsername, setNewUsername] = useState(user.username)
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [shouldRender, setShouldRender] = useState(open)
  const [isClosing, setIsClosing] = useState(false)

  useEffect(() => {
    setNewUsername(user.username)
  }, [user.username])

  useEffect(() => {
    if (open) {
      setShouldRender(true)
      setIsClosing(false)
      return
    }

    if (shouldRender) {
      setIsClosing(true)
      const timerId = window.setTimeout(() => {
        setShouldRender(false)
      }, 220)
      return () => window.clearTimeout(timerId)
    }
  }, [open, shouldRender])

  if (!shouldRender) {
    return null
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
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: newUsername.trim() }),
    })
    const body = await readAuthResponse(response)
    if (!response.ok || !body?.user) {
      onNotify(body?.message ?? 'Unable to update username.', 'error')
      return
    }
    onUserChange(body.user)
    setNewUsername(body.user.username)
    onNotify(body.message ?? 'Username updated.')
  }

  const handlePasswordChange = async () => {
    const response = await fetch('/api/auth/password', {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ currentPassword, newPassword }),
    })
    const body = await readAuthResponse(response)
    if (!response.ok) {
      onNotify(body?.message ?? 'Unable to change password.', 'error')
      return
    }
    setCurrentPassword('')
    setNewPassword('')
    onNotify(body?.message ?? 'Password changed.')
  }

  return (
    <div className={`profile-modal-backdrop ${isClosing ? 'closing' : 'open'}`} onClick={onClose}>
      <section className={`profile-modal menu-card ${isClosing ? 'closing' : 'open'}`} onClick={(event) => event.stopPropagation()}>
        <header>
          <h2>Profile</h2>
          <button type="button" className="close-btn" onClick={onClose} aria-label="Close profile">
            <FaTimes />
          </button>
        </header>

        <div className="identity">
          <p className="username">{user.username}</p>
          <p className="elo-line">
            <span>{elo ?? '—'}</span>
            <FaMedal aria-hidden="true" />
          </p>
        </div>
        <section className="settings">
          <h3>Account settings</h3>
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
        </section>
      </section>
    </div>
  )
}

export default ProfileModal
