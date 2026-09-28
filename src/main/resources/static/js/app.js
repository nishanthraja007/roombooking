import { API } from './api.js';
import { initParallax, attachCardTilt } from './parallax.js';

document.addEventListener('DOMContentLoaded', () => {
    initApp();
    initParallax();
});

let state = {
    rooms: [],
    guests: [],
    bookings: [],
    stats: null,
    isDateFiltered: false
};

async function initApp() {
    setupEventListeners();
    setDefaultDates();
    await refreshAllData();
}

function setDefaultDates() {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 2);

    const fromInput = document.getElementById('searchFromDate');
    const toInput = document.getElementById('searchToDate');

    if (fromInput && !fromInput.value) {
        fromInput.value = today.toISOString().split('T')[0];
    }
    if (toInput && !toInput.value) {
        toInput.value = tomorrow.toISOString().split('T')[0];
    }

    // Default Month/Year for Monthly View
    const monthSelect = document.getElementById('filterMonth');
    const yearInput = document.getElementById('filterYear');
    if (monthSelect) monthSelect.value = (today.getMonth() + 1).toString();
    if (yearInput) yearInput.value = today.getFullYear().toString();
}

function setupEventListeners() {
    // Quick Availability Search
    document.getElementById('availabilityForm')?.addEventListener('submit', handleSearchAvailability);

    // Live Room Filter in Rooms Directory
    document.getElementById('roomFilterInput')?.addEventListener('input', handleRoomFilterInput);
    document.getElementById('showAllRoomsBtn')?.addEventListener('click', handleResetRoomFilter);

    // Forms
    document.getElementById('addRoomForm')?.addEventListener('submit', handleAddRoom);
    document.getElementById('addGuestForm')?.addEventListener('submit', handleAddGuest);
    document.getElementById('createBookingForm')?.addEventListener('submit', handleCreateBooking);
    document.getElementById('editRoomForm')?.addEventListener('submit', handleEditRoom);
    document.getElementById('monthlyFilterForm')?.addEventListener('submit', handleFilterMonth);

    // Live Booking Cost Calculation Listener
    document.getElementById('bookingRoomSelect')?.addEventListener('change', updateBookingCostPreview);
    document.getElementById('bookingFromDate')?.addEventListener('change', updateBookingCostPreview);
    document.getElementById('bookingToDate')?.addEventListener('change', updateBookingCostPreview);

    // Modal Close
    document.querySelectorAll('.modal-close, .modal-cancel-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
        });
    });
}

async function refreshAllData() {
    try {
        const [rooms, guests, bookings, stats] = await Promise.all([
            API.getRooms(),
            API.getGuests(),
            API.getBookings(),
            API.getDashboardStats()
        ]);

        state.rooms = rooms;
        state.guests = guests;
        state.bookings = bookings;
        state.stats = stats;

        renderStats();
        renderRooms();
        renderGuests();
        renderBookings(state.bookings);
        populateDropdowns();
    } catch (err) {
        showToast(err.message || 'Failed to fetch data from backend', 'error');
    }
}

/* Render Dashboard Stats */
function renderStats() {
    if (!state.stats) return;
    document.getElementById('statTotalRooms').textContent = state.stats.totalRooms;
    document.getElementById('statTotalGuests').textContent = state.stats.totalGuests;
    document.getElementById('statTotalBookings').textContent = state.stats.totalBookings;
    document.getElementById('statConfirmedBookings').textContent = state.stats.confirmedBookings;
    document.getElementById('statCancelledBookings').textContent = state.stats.cancelledBookings;
}

/* Render Rooms */
function renderRooms(roomsToRender = state.rooms) {
    const grid = document.getElementById('roomsGrid');
    if (!grid) return;

    // Update count badge and filter status text
    const badge = document.getElementById('roomCountBadge');
    if (badge) {
        badge.textContent = `${roomsToRender.length} Room${roomsToRender.length !== 1 ? 's' : ''}`;
    }

    const resetBtn = document.getElementById('showAllRoomsBtn');
    const statusText = document.getElementById('roomFilterStatus');
    if (state.isDateFiltered) {
        if (resetBtn) resetBtn.style.display = 'inline-block';
        if (statusText) statusText.textContent = `Showing available rooms for selected dates`;
    } else {
        if (resetBtn) resetBtn.style.display = 'none';
        if (statusText) statusText.textContent = `Showing all guest house rooms`;
    }

    if (roomsToRender.length === 0) {
        grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: var(--text-secondary); padding: 40px;">
            <p style="font-size: 1.1rem; margin-bottom: 8px;">No rooms found.</p>
            <p style="font-size: 0.85rem; color: var(--text-muted);">Try clearing your filter or selecting different dates.</p>
        </div>`;
        return;
    }

    grid.innerHTML = roomsToRender.map(room => `
        <div class="room-card" data-room-id="${room.id}">
            <div class="glare-effect"></div>
            <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:4px;">
                <div class="room-type">${escapeHtml(room.type)}</div>
                <div style="display:flex; gap:6px;">
                    <button class="btn-icon-edit" title="Edit Room" onclick="openEditRoomModal(${room.id})" style="background:rgba(139,92,246,0.15);border:1px solid rgba(139,92,246,0.35);color:#a78bfa;border-radius:8px;padding:5px 9px;cursor:pointer;font-size:0.8rem;transition:all 0.2s;">&#9998;</button>
                    <button class="btn-icon-del" title="Delete Room" onclick="deleteRoom(${room.id})" style="background:rgba(239,68,68,0.12);border:1px solid rgba(239,68,68,0.3);color:#ef4444;border-radius:8px;padding:5px 9px;cursor:pointer;font-size:0.8rem;transition:all 0.2s;">&#128465;</button>
                </div>
            </div>
            <div class="room-info-pill">Room #${room.id} &bull; Max ${room.capacity} Guests</div>
            <div class="room-price">&#8377;${room.nightlyRate} <span>/ night</span></div>
            <button class="btn-primary" style="width: 100%; font-size: 0.85rem; margin-top:4px;" onclick="openCreateBookingModal(${room.id})">Book Room</button>
        </div>
    `).join('');

    attachCardTilt();
}

function handleRoomFilterInput(e) {
    const term = e.target.value.toLowerCase().trim();
    const sourceList = state.availableRoomsList || state.rooms;
    if (!term) {
        renderRooms(sourceList);
        return;
    }
    const filtered = sourceList.filter(r => 
        r.type.toLowerCase().includes(term) || 
        String(r.id).includes(term) ||
        String(r.capacity).includes(term)
    );
    renderRooms(filtered);
}

function handleResetRoomFilter() {
    state.isDateFiltered = false;
    state.availableRoomsList = null;
    const filterInput = document.getElementById('roomFilterInput');
    if (filterInput) filterInput.value = '';
    renderRooms(state.rooms);
    showToast('Showing all guest house rooms', 'info');
}

/* Render Guests */
function renderGuests() {
    const tbody = document.getElementById('guestsTableBody');
    if (!tbody) return;

    if (state.guests.length === 0) {
        tbody.innerHTML = `<tr><td colspan="3" style="text-align:center; color: var(--text-secondary);">No guests registered yet.</td></tr>`;
        return;
    }

    tbody.innerHTML = state.guests.map(guest => `
        <tr>
            <td>#${guest.id}</td>
            <td><strong>${escapeHtml(guest.name)}</strong></td>
            <td>${escapeHtml(guest.phone)}</td>
        </tr>
    `).join('');
}

/* Render Bookings List */
function renderBookings(bookingsList) {
    const tbody = document.getElementById('bookingsTableBody');
    if (!tbody) return;

    if (bookingsList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--text-secondary); padding: 24px;">No bookings found.</td></tr>`;
        return;
    }

    tbody.innerHTML = bookingsList.map(b => `
        <tr>
            <td>#${b.id}</td>
            <td>Room #${b.roomId} (${escapeHtml(b.roomType || '')})</td>
            <td>${escapeHtml(b.guestName || 'Guest #' + b.guestId)}</td>
            <td>${b.fromDate} to ${b.toDate}</td>
            <td><strong>&#8377;${b.totalCost}</strong></td>
            <td>
                <span class="badge ${b.status === 'CONFIRMED' ? 'badge-confirmed' : 'badge-cancelled'}">
                    ${b.status}
                </span>
            </td>
            <td>
                ${b.status === 'CONFIRMED' ? `
                    <button class="btn-danger" onclick="cancelBooking(${b.id})">Cancel</button>
                ` : `<span style="color: var(--text-muted); font-size: 0.8rem;">Cancelled</span>`}
            </td>
        </tr>
    `).join('');
}

/* Populate Form Dropdowns */
function populateDropdowns() {
    const roomSelect = document.getElementById('bookingRoomSelect');
    const guestSelect = document.getElementById('bookingGuestSelect');

    if (roomSelect) {
        roomSelect.innerHTML = `<option value="">Select Room</option>` +
            state.rooms.map(r => `<option value="${r.id}">Room #${r.id} - ${r.type} (&#8377;${r.nightlyRate}/night)</option>`).join('');
    }

    if (guestSelect) {
        guestSelect.innerHTML = `<option value="">Select Guest</option>` +
            state.guests.map(g => `<option value="${g.id}">${g.name} (${g.phone})</option>`).join('');
    }
}

/* Search Availability Event Handler */
async function handleSearchAvailability(e) {
    e.preventDefault();
    const fromDate = document.getElementById('searchFromDate').value;
    const toDate = document.getElementById('searchToDate').value;

    if (!fromDate || !toDate) {
        showToast('Please select both From Date and To Date', 'error');
        return;
    }

    try {
        const availableRooms = await API.getAvailableRooms(fromDate, toDate);
        state.isDateFiltered = true;
        state.availableRoomsList = availableRooms;
        const filterInput = document.getElementById('roomFilterInput');
        if (filterInput) filterInput.value = '';
        renderRooms(availableRooms);
        showToast(`Found ${availableRooms.length} available room(s) for date range`, 'success');
        document.getElementById('roomsSection')?.scrollIntoView({ behavior: 'smooth' });
    } catch (err) {
        showToast(err.message || 'Error checking availability', 'error');
    }
}

/* Add Room Handler */
async function handleAddRoom(e) {
    e.preventDefault();
    const type = document.getElementById('roomType').value.trim();
    const capacity = parseInt(document.getElementById('roomCapacity').value);
    const nightlyRate = parseFloat(document.getElementById('roomRate').value);

    try {
        await API.createRoom({ type, capacity, nightlyRate });
        showToast('Room created successfully', 'success');
        document.getElementById('addRoomForm').reset();
        await refreshAllData();
    } catch (err) {
        showToast(err.message || 'Failed to create room', 'error');
    }
}

/* Add Guest Handler */
async function handleAddGuest(e) {
    e.preventDefault();
    const name = document.getElementById('guestName').value.trim();
    const phone = document.getElementById('guestPhone').value.trim();

    try {
        await API.createGuest({ name, phone });
        showToast('Guest registered successfully', 'success');
        document.getElementById('addGuestForm').reset();
        await refreshAllData();
    } catch (err) {
        showToast(err.message || 'Failed to create guest', 'error');
    }
}

/* Live Booking Cost Preview */
function updateBookingCostPreview() {
    const roomId = document.getElementById('bookingRoomSelect')?.value;
    const fromDateStr = document.getElementById('bookingFromDate')?.value;
    const toDateStr = document.getElementById('bookingToDate')?.value;
    const preview = document.getElementById('totalCostPreview');

    if (!roomId || !fromDateStr || !toDateStr || !preview) return;

    const room = state.rooms.find(r => r.id == roomId);
    if (!room) return;

    const fromDate = new Date(fromDateStr);
    const toDate = new Date(toDateStr);

    const diffTime = toDate - fromDate;
    const nights = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (nights > 0) {
        const total = nights * room.nightlyRate;
        preview.textContent = `Total Cost: ₹${total} (${nights} night${nights > 1 ? 's' : ''})`;
        preview.style.color = 'var(--accent)';
    } else {
        preview.textContent = 'Invalid dates (From Date must be before To Date)';
        preview.style.color = '#ef4444';
    }
}

/* Create Booking Handler */
async function handleCreateBooking(e) {
    e.preventDefault();
    const roomId = parseInt(document.getElementById('bookingRoomSelect').value);
    const guestId = parseInt(document.getElementById('bookingGuestSelect').value);
    const fromDate = document.getElementById('bookingFromDate').value;
    const toDate = document.getElementById('bookingToDate').value;

    const modalContent = document.querySelector('#bookingModal .modal-content');

    try {
        await API.createBooking({ roomId, guestId, fromDate, toDate });
        showToast('Booking CONFIRMED successfully!', 'success');
        document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
        await refreshAllData();
    } catch (err) {
        // HTTP 409 Conflict Shake Animation & Red Toast Notification
        if (err.status === 409) {
            if (modalContent) {
                modalContent.classList.add('shake');
                setTimeout(() => modalContent.classList.remove('shake'), 400);
            }
        }
        showToast(err.message || 'Failed to create booking', 'error');
    }
}

/* Cancel Booking Handler */
window.cancelBooking = async function(bookingId) {
    if (!confirm(`Are you sure you want to cancel booking #${bookingId}?`)) return;

    try {
        await API.cancelBooking(bookingId);
        showToast(`Booking #${bookingId} has been CANCELLED`, 'success');
        await refreshAllData();
    } catch (err) {
        showToast(err.message || 'Failed to cancel booking', 'error');
    }
};

/* Monthly View Filter */
async function handleFilterMonth(e) {
    e.preventDefault();
    const year = parseInt(document.getElementById('filterYear').value);
    const month = parseInt(document.getElementById('filterMonth').value);

    try {
        const monthlyBookings = await API.getBookingsForMonth(year, month);
        renderBookings(monthlyBookings);
        showToast(`Loaded ${monthlyBookings.length} booking(s) for ${year}-${month < 10 ? '0' + month : month}`, 'success');
    } catch (err) {
        showToast(err.message || 'Error fetching monthly bookings', 'error');
    }
}

/* Open Create Booking Modal helper */
window.openCreateBookingModal = function(roomId = null) {
    const modal = document.getElementById('bookingModal');
    if (!modal) return;

    if (roomId) {
        document.getElementById('bookingRoomSelect').value = roomId;
    }

    const searchFrom = document.getElementById('searchFromDate')?.value;
    const searchTo = document.getElementById('searchToDate')?.value;

    if (searchFrom && searchTo) {
        document.getElementById('bookingFromDate').value = searchFrom;
        document.getElementById('bookingToDate').value = searchTo;
    } else {
        const today = new Date();
        const tomorrow = new Date(today);
        tomorrow.setDate(tomorrow.getDate() + 2);
        document.getElementById('bookingFromDate').value = today.toISOString().split('T')[0];
        document.getElementById('bookingToDate').value = tomorrow.toISOString().split('T')[0];
    }

    updateBookingCostPreview();
    modal.classList.add('active');
};

/* Open Edit Room Modal */
window.openEditRoomModal = function(roomId) {
    const room = state.rooms.find(r => r.id === roomId);
    if (!room) return;

    const modal = document.getElementById('editRoomModal');
    if (!modal) return;

    document.getElementById('editRoomId').value = room.id;
    document.getElementById('editRoomType').value = room.type;
    document.getElementById('editRoomCapacity').value = room.capacity;
    document.getElementById('editRoomRate').value = room.nightlyRate;

    modal.classList.add('active');
};

/* Delete Room Handler */
window.deleteRoom = async function(roomId) {
    if (!confirm(`Are you sure you want to delete Room #${roomId}? This action cannot be undone.`)) return;

    try {
        await API.deleteRoom(roomId);
        showToast(`Room #${roomId} deleted successfully`, 'success');
        await refreshAllData();
    } catch (err) {
        showToast(err.message || 'Failed to delete room', 'error');
    }
};

/* Edit Room Form Submit */
async function handleEditRoom(e) {
    e.preventDefault();
    const id = parseInt(document.getElementById('editRoomId').value);
    const type = document.getElementById('editRoomType').value.trim();
    const capacity = parseInt(document.getElementById('editRoomCapacity').value);
    const nightlyRate = parseFloat(document.getElementById('editRoomRate').value);

    try {
        await API.updateRoom(id, { type, capacity, nightlyRate });
        showToast(`Room #${id} updated successfully`, 'success');
        document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
        await refreshAllData();
    } catch (err) {
        showToast(err.message || 'Failed to update room', 'error');
    }
}

/* Toast Notification Helper */
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <span style="font-size: 1.2rem;">${type === 'error' ? '&#9888;' : '&#10004;'}</span>
        <div>${escapeHtml(message)}</div>
    `;

    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(50px)';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function escapeHtml(str) {
    return String(str).replace(/[&<>"']/g, function(m) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
    });
}
