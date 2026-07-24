    function toggleUserMenu() {
        document.getElementById('userDropdown').classList.toggle('active');
    }
    document.addEventListener('click', function(e) {
        var menu = document.querySelector('.user-menu');
        if (menu && !menu.contains(e.target)) {
            document.getElementById('userDropdown').classList.remove('active');
        }
    });