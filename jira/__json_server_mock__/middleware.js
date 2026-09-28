const fs = require('fs');
const path = require('path');
const registeredUsersPath = path.join(__dirname, 'registered-users.json');

const readRegisteredUsers = () => {
    try {
        if (!fs.existsSync(registeredUsersPath)) {
            fs.writeFileSync(registeredUsersPath, JSON.stringify([], null, 2), 'utf-8');
            return [];
        }
        const data = fs.readFileSync(registeredUsersPath, 'utf-8');
        return JSON.parse(data);
    } catch (error) {
        console.error('读取注册用户文件失败:', error);
        return [];
    }
};
const writeRegisteredUsers = (data) => {
    try {
        fs.writeFileSync(registeredUsersPath, JSON.stringify(data, null, 2), 'utf-8');
    } catch (error) {
        console.error('写入注册用户文件失败:', error);
    }
};
module.exports = (req, res, next) => {
    const origin = req.headers.origin;
    if (origin && origin.startsWith('http://localhost:')) {
        res.header('Access-Control-Allow-Origin', origin);
    }
    res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization');
    res.header('Access-Control-Allow-Credentials', 'true');
    
    if (req.method === 'OPTIONS') {
        return res.sendStatus(200);
    }
    if (req.method === 'POST' && req.path === '/login') {
        const registeredUsers = readRegisteredUsers();
        let user = registeredUsers.find(u => u.username === req.body.username && u.password === req.body.password);
        if (!user) {
            const dbPath = path.join(__dirname, 'db.json');
            const dbData = JSON.parse(fs.readFileSync(dbPath, 'utf-8'));
            user = dbData.users.find(u => u.username === req.body.username && u.password === req.body.password);
        }
        
        if (user) {
            return res.status(200).json({
                user: {
                    id: user.id,
                    name: user.name || user.username,
                    token: 'token_' + user.id + '_' + Date.now()
                }
            });
        } else {
            return res.status(401).json({
                message: '用户名或密码错误'
            });
        }
    }
if (req.method === 'POST' && req.path === '/register') {
        const { username, password } = req.body;
        
        if (!username || !password) {
            return res.status(400).json({
                message: '用户名和密码不能为空'
            });
        }
        
        if (typeof username !== 'string' || typeof password !== 'string') {
            return res.status(400).json({
                message: '用户名和密码格式不正确'
            });
        }
        
        if (username.trim() === '' || password.trim() === '') {
            return res.status(400).json({
                message: '用户名和密码不能为空'
            });
        }
        
        const registeredUsers = readRegisteredUsers();
        const existingUser = registeredUsers.find(u => u.username === req.body.username);
        
        if (existingUser) {
            return res.status(400).json({
                message: '用户名已存在'
            });
        }
        const dbPath = path.join(__dirname, 'db.json');
        const dbData = JSON.parse(fs.readFileSync(dbPath, 'utf-8'));
        const newUser = {
            id: registeredUsers.length > 0 ? Math.max(...registeredUsers.map(u => u.id)) + 1 : (dbData.users.length + 1),
            username: req.body.username,
            password: req.body.password,
            name: req.body.username,
            email: '',
            title: '',
            organization: '',
            createdAt: new Date().toISOString()
        };
        registeredUsers.push(newUser);
        writeRegisteredUsers(registeredUsers);
        
        return res.status(200).json({
            user: {
                id: newUser.id,
                name: newUser.name,
                token: 'token_' + newUser.id + '_' + Date.now()
            }
        });
    }
    
    if (req.method === 'GET' && req.path === '/me') {
        const authHeader = req.headers.authorization;
        
        if (!authHeader || !authHeader.startsWith('Bearer ')) {
            return res.status(401).json({
                message: '未提供认证令牌'
            });
        }
        
        const token = authHeader.substring(7);
        
        const registeredUsers = readRegisteredUsers();
        let user = null;
        
        for (const registeredUser of registeredUsers) {
            const expectedToken = 'token_' + registeredUser.id + '_';
            if (token.startsWith(expectedToken)) {
                user = registeredUser;
                break;
            }
        }
        
        if (!user) {
            const dbPath = path.join(__dirname, 'db.json');
            const dbData = JSON.parse(fs.readFileSync(dbPath, 'utf-8'));
            
            for (const dbUser of dbData.users) {
                const expectedToken = 'token_' + dbUser.id + '_';
                if (token.startsWith(expectedToken)) {
                    user = dbUser;
                    break;
                }
            }
        }
        
        if (user) {
            return res.status(200).json({
                user: {
                    id: user.id,
                    name: user.name || user.username,
                    token: token
                }
            });
        } else {
            return res.status(401).json({
                message: '无效的认证令牌'
            });
        }
    }
    
    if (req.method === 'GET' && req.path === '/tasks') {
        const dbPath = path.join(__dirname, 'db.json');
        const dbData = JSON.parse(fs.readFileSync(dbPath, 'utf-8'));
        let tasks = dbData.tasks;
        
        const name = req.query.name;
        const projectId = req.query.projectId;
        const processorId = req.query.processorId;
        const typeId = req.query.typeId;
        
        if (name) {
            tasks = tasks.filter(task => 
                task.name && task.name.toLowerCase().includes(name.toLowerCase())
            );
        }
        
        if (projectId) {
            tasks = tasks.filter(task => task.projectId === Number(projectId));
        }
        
        if (processorId) {
            tasks = tasks.filter(task => task.processorId === Number(processorId));
        }
        
        if (typeId) {
            tasks = tasks.filter(task => task.typeId === Number(typeId));
        }
        
        return res.status(200).json(tasks);
    }
    
    if (req.method === 'GET' && req.path === '/projects') {
        const dbPath = path.join(__dirname, 'db.json');
        const dbData = JSON.parse(fs.readFileSync(dbPath, 'utf-8'));
        let projects = dbData.projects;
        
        const name = req.query.name;
        const personId = req.query.personId;
        
        if (name) {
            projects = projects.filter(project => 
                project.name && project.name.toLowerCase().includes(name.toLowerCase())
            );
        }
        
        if (personId) {
            projects = projects.filter(project => project.personId === Number(personId));
        }
        
        return res.status(200).json(projects);
    }
    
    if (req.method === 'POST' && req.path === '/kanbans/reorder') {
        const dbPath = path.join(__dirname, 'db.json');
        const dbData = JSON.parse(fs.readFileSync(dbPath, 'utf-8'));
        const kanbans = dbData.kanbans;
        
        const { fromId, referenceId, type } = req.body;
        
        const fromIndex = kanbans.findIndex(k => k.id === fromId);
        const toIndex = kanbans.findIndex(k => k.id === referenceId);
        
        if (fromIndex === -1 || toIndex === -1) {
            return res.status(400).json({
                message: '看板不存在'
            });
        }
        
        // 移除要移动的看板
        const [movedKanban] = kanbans.splice(fromIndex, 1);
        
        // 重新插入到目标位置
        const newToIndex = type === 'after' ? toIndex + 1 : toIndex;
        kanbans.splice(newToIndex, 0, movedKanban);
        
        // 更新排序
        kanbans.forEach((kanban, index) => {
            kanban.order = index + 1;
        });
        
        // 保存回文件
        fs.writeFileSync(dbPath, JSON.stringify(dbData, null, 2), 'utf-8');
        
        return res.status(200).json(kanbans);
    }
    
    if (req.method === 'POST' && req.path === '/tasks/reorder') {
        const dbPath = path.join(__dirname, 'db.json');
        const dbData = JSON.parse(fs.readFileSync(dbPath, 'utf-8'));
        const tasks = dbData.tasks;
        
        const { fromId, referenceId, type, fromKanbanId, toKanbanId } = req.body;
        
        // 找到要移动的任务
        const fromTaskIndex = tasks.findIndex(t => t.id === fromId);
        if (fromTaskIndex === -1) {
            return res.status(400).json({
                message: '任务不存在'
            });
        }
        
        // 更新任务的看板ID
        const movedTask = tasks[fromTaskIndex];
        movedTask.kanbanId = toKanbanId;
        
        // 移除要移动的任务
        tasks.splice(fromTaskIndex, 1);
        
        // 找到目标位置
        let toTaskIndex = -1;
        if (referenceId) {
            toTaskIndex = tasks.findIndex(t => t.id === referenceId);
        }
        
        // 重新插入到目标位置
        if (toTaskIndex !== -1) {
            const newIndex = type === 'after' ? toTaskIndex + 1 : toTaskIndex;
            tasks.splice(newIndex, 0, movedTask);
        } else {
            // 如果没有参考任务，添加到目标看板的末尾
            tasks.push(movedTask);
        }
        
        // 保存回文件
        fs.writeFileSync(dbPath, JSON.stringify(dbData, null, 2), 'utf-8');
        
        return res.status(200).json(tasks);
    }
    
    return next();
}