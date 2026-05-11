import { useEffect, useRef, useState } from 'react'
import { FaRegUserCircle } from 'react-icons/fa'
import type { AuthResponse, User } from '../../types/auth'
import { authHeaders } from '../../utils/auth'
import StarRating from './StarRating'
import logoPiece from '../../assets/logo.svg'
import '../../css/menu/menu-header.css'

type MenuHeaderProps = {
  user: User
  elo: number | null
  averageRating: number | null
  onUserChange: (user: User) => void
  onNotify: (message: string, kind?: 'ok' | 'error') => void
  onLogout: () => void
}

function MenuHeader({ user, elo, averageRating, onUserChange, onNotify, onLogout }: MenuHeaderProps) {
  const [dropdownOpen, setDropdownOpen] = useState(false)
  const [newUsername, setNewUsername] = useState(user.username)
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const wrapRef = useRef<HTMLDivElement>(null)

  useEffect(() => { setNewUsername(user.username) }, [user.username])

  useEffect(() => {
    if (!dropdownOpen) return
    const handler = (e: MouseEvent) => {
      if (wrapRef.current && !wrapRef.current.contains(e.target as Node))
        setDropdownOpen(false)
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [dropdownOpen])

  const readAuth = async (r: Response): Promise<AuthResponse | null> => {
    try { return (await r.json()) as AuthResponse } catch { return null }
  }

  const handleUsernameSave = async () => {
    const r = await fetch('/api/auth/profile', {
      method: 'PUT',
      headers: { ...authHeaders(), 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: newUsername.trim() }),
    })
    const body = await readAuth(r)
    if (!r.ok || !body?.user) { onNotify(body?.message ?? 'Unable to update username.', 'error'); return }
    onUserChange(body.user)
    setNewUsername(body.user.username)
    onNotify(body.message ?? 'Username updated.')
  }

  const handlePasswordChange = async () => {
    const r = await fetch('/api/auth/password', {
      method: 'POST',
      headers: { ...authHeaders(), 'Content-Type': 'application/json' },
      body: JSON.stringify({ currentPassword, newPassword }),
    })
    const body = await readAuth(r)
    if (!r.ok) { onNotify(body?.message ?? 'Unable to change password.', 'error'); return }
    setCurrentPassword('')
    setNewPassword('')
    onNotify(body?.message ?? 'Password changed.')
  }

  return (
    <header className="menu-header-line menu-card">
      <div className="brand-group">
        <div className="brand-block">
          <h1>
            <img src={logoPiece} alt="Chess piece logo" />
          </h1>
        </div>

        <div className="header-rating">
          <span className="header-label">Average game rating</span>
          <StarRating value={averageRating} labelPrefix="Average rating" />
        </div>
      </div>

      <div className="header-actions">
        <div className="profile-btn-wrap" ref={wrapRef}>
          <button
            type="button"
            className={`profile-open-btn ${dropdownOpen ? 'active' : ''}`}
            onClick={() => setDropdownOpen((v) => !v)}
          >
            <FaRegUserCircle aria-hidden="true" className="profile-icon" />
            <span>{user.username}</span>
          </button>

          {dropdownOpen && (
            <div className="profile-dropdown">
              <div className="pd-identity">
                <span className="pd-name">{user.username}</span>
                {elo !== null && <span className="pd-elo">{elo} ELO</span>}
              </div>

              <div className="pd-section">
                <label className="pd-label">
                  <span>Username</span>
                  <input
                    value={newUsername}
                    onChange={(e) => setNewUsername(e.target.value)}
                    minLength={3}
                  />
                </label>
                <button type="button" className="pd-btn primary" onClick={() => void handleUsernameSave()}>
                  Save name
                </button>
              </div>

              <div className="pd-section">
                <label className="pd-label">
                  <span>Current password</span>
                  <input
                    type="password"
                    value={currentPassword}
                    onChange={(e) => setCurrentPassword(e.target.value)}
                    minLength={6}
                  />
                </label>
                <label className="pd-label">
                  <span>New password</span>
                  <input
                    type="password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    minLength={6}
                  />
                </label>
                <button type="button" className="pd-btn" onClick={() => void handlePasswordChange()}>
                  Change password
                </button>
              </div>
            </div>
          )}
        </div>

        <button type="button" className="header-logout-btn" onClick={onLogout}>
          Log out
        </button>
      </div>
    </header>
  )
}

export default MenuHeader
